package org.briarproject.briar.android.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.briarproject.briar.R;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import static java.util.Objects.requireNonNull;
import static org.briarproject.briar.android.AppModule.getAndroidComponent;
import static org.briarproject.briar.android.settings.SettingsActivity.enableAndPersist;
import static org.briarproject.briar.android.util.UiUtils.hasScreenLock;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SecurityFragment extends PreferenceFragmentCompat {

	public static final String PREF_SCREEN_LOCK = "pref_key_lock";
	public static final String PREF_SCREEN_LOCK_TIMEOUT = "pref_key_lock_timeout";

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private SettingsViewModel viewModel;
	private SwitchPreferenceCompat screenLock;
	private ListPreference screenLockTimeout;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		getAndroidComponent(context).inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(SettingsViewModel.class);
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_security);
		getPreferenceManager().setPreferenceDataStore(viewModel.settingsStore);

		screenLock = findPreference(PREF_SCREEN_LOCK);
		screenLockTimeout = findPreference(PREF_SCREEN_LOCK_TIMEOUT);

		if (screenLockTimeout == null) return;

		// ВАЖНО: Защита от null в массивах
		fixNullValues();

		// Полностью заменяем стандартный диалог на кастомный с темой от SettingsActivity
		screenLockTimeout.setOnPreferenceClickListener(preference -> {
			showCustomDialogWithActivityTheme();
			return true; // Блокируем стандартный диалог
		});

		screenLockTimeout.setSummaryProvider(preference -> {
			try {
				String timeout = screenLockTimeout.getValue();
				if (timeout == null) return getString(R.string.pref_lock_timeout_title);

				CharSequence entry = screenLockTimeout.getEntry();
				if (entry == null) return getString(R.string.pref_lock_timeout_title);

				return getString(R.string.pref_lock_timeout_summary, entry);
			} catch (Exception e) {
				return getString(R.string.pref_lock_timeout_title);
			}
		});
	}

	private void showCustomDialogWithActivityTheme() {
		if (screenLockTimeout == null) return;

		CharSequence[] entries = screenLockTimeout.getEntries();
		CharSequence[] values = screenLockTimeout.getEntryValues();
		String currentValue = screenLockTimeout.getValue();

		if (entries == null || values == null || entries.length == 0) {
			return;
		}

		// Находим текущий выбранный элемент
		int checkedItem = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null && values[i].toString().equals(currentValue)) {
				checkedItem = i;
				break;
			}
		}

		// Используем тему из SettingsActivity (наследуется от BriarActivity)
		// Просто создаем диалог без явного указания темы - он унаследует тему активности
		androidx.appcompat.app.AlertDialog.Builder builder =
				new androidx.appcompat.app.AlertDialog.Builder(requireContext());

		builder.setTitle(screenLockTimeout.getTitle());

		builder.setSingleChoiceItems(entries, checkedItem, (dialog, which) -> {
			if (which >= 0 && which < values.length && values[which] != null) {
				String selectedValue = values[which].toString();
				screenLockTimeout.setValue(selectedValue);
				viewModel.settingsStore.putString(PREF_SCREEN_LOCK_TIMEOUT, selectedValue);
				enableAndPersist(screenLockTimeout);
			}
			dialog.dismiss();
		});

		builder.setNegativeButton(android.R.string.cancel, null);

		// Показываем диалог
		builder.show();
	}

	private void fixNullValues() {
		if (screenLockTimeout == null) return;

		// Простая проверка и установка дефолтных значений если есть проблемы
		CharSequence[] entries = screenLockTimeout.getEntries();
		CharSequence[] values = screenLockTimeout.getEntryValues();

		// Если есть null значения, заменяем их на гарантированно рабочие
		if (entries == null || values == null ||
				entries.length == 0 || values.length == 0 ||
				hasNullValues(entries) || hasNullValues(values)) {

			// Используем правильные значения из вашего arrays.xml
			CharSequence[] safeEntries = {
					"Immediately",
					"After 1 minute",
					"After 5 minutes",
					"After 15 minutes",
					"After 1 hour"
			};

			CharSequence[] safeValues = {
					"0", "1", "5", "15", "60"
			};

			screenLockTimeout.setEntries(safeEntries);
			screenLockTimeout.setEntryValues(safeValues);
		}

		// Устанавливаем значение по умолчанию
		if (screenLockTimeout.getValue() == null) {
			screenLockTimeout.setValue("5");
		}
	}

	private boolean hasNullValues(CharSequence[] array) {
		if (array == null) return true;
		for (CharSequence item : array) {
			if (item == null) return true;
		}
		return false;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (screenLockTimeout == null) return;

		LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
		viewModel.getScreenLockTimeout().observe(lifecycleOwner, value -> {
			if (value != null) {
				screenLockTimeout.setValue(value);
				enableAndPersist(screenLockTimeout);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.security_settings_title);
		checkScreenLock();
	}

	private void checkScreenLock() {
		LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
		viewModel.getScreenLockEnabled().removeObservers(lifecycleOwner);
		if (hasScreenLock(requireActivity())) {
			viewModel.getScreenLockEnabled().observe(lifecycleOwner, on -> {
				if (on != null) {
					screenLock.setChecked(on);
					enableAndPersist(screenLock);
				}
			});
			screenLock.setSummary(R.string.pref_lock_summary);
		} else {
			screenLock.setEnabled(false);
			screenLock.setPersistent(false);
			screenLock.setChecked(false);
			screenLock.setSummary(R.string.pref_lock_disabled_summary);
		}
	}
}