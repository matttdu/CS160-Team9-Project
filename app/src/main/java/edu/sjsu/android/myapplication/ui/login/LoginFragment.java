package edu.sjsu.android.myapplication.ui.login;
import edu.sjsu.android.myapplication.SQLiteController;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.util.Log;


import edu.sjsu.android.myapplication.databinding.FragmentLoginBinding;

import edu.sjsu.android.myapplication.R;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    private SQLiteController db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Init DB controller
        db = SQLiteController.dbInstance(getContext());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final Button registerButton = binding.goRegister;
        final ProgressBar loadingProgressBar = binding.loading;

        // For form validation
        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) return;

                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        // For login results
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) return;
                loadingProgressBar.setVisibility(View.GONE);

                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }

                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
            }
        });

        // Enable login button only if fields are not empty
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                loginButton.setEnabled(!usernameEditText.getText().toString().trim().isEmpty()
                        && !passwordEditText.getText().toString().trim().isEmpty());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        // Handle the enter button for login
        passwordEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginUser(view, usernameEditText.getText().toString(), passwordEditText.getText().toString(), loadingProgressBar);
            }
            return false;
        });

        // Handle the login button
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
        
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }
        
            loadingProgressBar.setVisibility(View.VISIBLE);
            boolean success = db.loginUser(username, password); // check by username + password
            loadingProgressBar.setVisibility(View.GONE);
        
            if (success) {
                // Save the logged-in username in SharedPreferences so that it’s available to all fragments.
                requireContext()
                        .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("loggedInUser", username)
                        .apply();

                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();


                Navigation.findNavController(v).navigate(R.id.successful_login);
            } else {
                Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle the registration button
        registerButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_register);
        });
    }

    // Actual login logic using SQLiteController
    private void loginUser(View view, String username, String password, ProgressBar loadingBar) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        boolean loggedIn = db.loginUser(username, password);
        loadingBar.setVisibility(View.GONE);


        if (loggedIn) {
            Navigation.findNavController(view).navigate(R.id.successful_login);
        } else {
            Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getContext().getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}