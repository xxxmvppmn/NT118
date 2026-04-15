package com.example.waviapp.managers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.TaiKhoan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSessionManager {
    private static UserSessionManager instance;
    private final MutableLiveData<TaiKhoan> userLiveData = new MutableLiveData<>();
    private final DatabaseHelper dbHelper = new DatabaseHelper();

    private UserSessionManager() {}

    public static synchronized UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    public LiveData<TaiKhoan> getUserLiveData() {
        return userLiveData;
    }

    public TaiKhoan getUserData() {
        return userLiveData.getValue();
    }

    public void fetchUserData(String userId, SimpleCallback callback) {
        dbHelper.getUser(userId, new DatabaseHelper.UserCallback() {
            @Override
            public void onSuccess(TaiKhoan user) {
                userLiveData.setValue(user);
                if (callback != null) callback.onComplete(true);
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onComplete(false);
            }
        });
    }

    public void updateUserDataLocally(TaiKhoan updatedUser) {
        userLiveData.setValue(updatedUser);
    }

    public void clearSession() {
        userLiveData.setValue(null);
    }

    public interface SimpleCallback {
        void onComplete(boolean success);
    }
}