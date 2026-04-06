package com.example.waviapp.firebase;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    public static final int RC_GOOGLE_SIGN_IN = 9001;

    private final FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Khởi tạo Google Sign-In client
     */
    public void initGoogleSignIn(Activity activity, String webClientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    /**
     * Lấy Intent để bắt đầu Google Sign-In flow
     */
    public Intent getGoogleSignInIntent() {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
            return mGoogleSignInClient.getSignInIntent();
        }
        return null;
    }

    /**
     * Xử lý kết quả từ Google Sign-In
     */
    public void handleGoogleSignInResult(Task<GoogleSignInAccount> task, AuthCallback callback) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken(), callback);
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            callback.onFailure("Đăng nhập Google thất bại: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure("Xác thực Google thất bại");
                    }
                });
    }

    /**
     * Đăng ký tài khoản bằng Email/Password
     */
    public void register(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        String errorMsg = "Đăng ký thất bại";
                        if (task.getException() != null) {
                            String msg = task.getException().getMessage();
                            if (msg != null && msg.contains("email address is already in use")) {
                                errorMsg = "Email này đã được sử dụng";
                            } else if (msg != null && msg.contains("badly formatted")) {
                                errorMsg = "Email không đúng định dạng";
                            } else if (msg != null && msg.contains("at least 6 characters")) {
                                errorMsg = "Mật khẩu phải có ít nhất 6 ký tự";
                            } else {
                                errorMsg = msg;
                            }
                        }
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Đăng nhập bằng Email/Password
     */
    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        String errorMsg = "Đăng nhập thất bại";
                        if (task.getException() != null) {
                            String msg = task.getException().getMessage();
                            if (msg != null && msg.contains("no user record")) {
                                errorMsg = "Tài khoản không tồn tại";
                            } else if (msg != null && msg.contains("password is invalid")) {
                                errorMsg = "Mật khẩu không đúng";
                            } else if (msg != null && msg.contains("blocked all requests")) {
                                errorMsg = "Quá nhiều lần thử. Vui lòng thử lại sau";
                            } else {
                                errorMsg = msg;
                            }
                        }
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        // 1. Đăng xuất khỏi Firebase
        mAuth.signOut();

        // 2. Xử lý Google Sign-In Client
        if (mGoogleSignInClient != null) {
            // signOut() chỉ là đăng xuất,
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                mGoogleSignInClient.revokeAccess();
            });
        }
    }

    /**
     * Lấy user đang đăng nhập
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Kiểm tra đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Đổi mật khẩu
     */
    public void changePassword(String currentPassword, String newPassword, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onFailure("Chưa đăng nhập");
            return;
        }

        // Re-authenticate trước khi đổi mật khẩu
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        callback.onSuccess(user);
                                    } else {
                                        callback.onFailure("Cập nhật mật khẩu thất bại");
                                    }
                                });
                    } else {
                        callback.onFailure("Mật khẩu hiện tại không đúng");
                    }
                });
    }

    /**
     * Xóa tài khoản
     */
    public void deleteAccount(AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Chưa đăng nhập");
            return;
        }

        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure("Xóa tài khoản thất bại. Vui lòng đăng nhập lại và thử lại.");
                    }
                });
    }
    /**
     * Quên mật khẩu
     */
    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Thành công: Firebase đã gửi mail
                        callback.onSuccess(null);
                    } else {
                        // Thất bại: Thường là do Email không tồn tại
                        String error = "Lỗi: ";
                        if (task.getException() != null) {
                            String msg = task.getException().getMessage();
                            if (msg != null && msg.contains("no user record")) {
                                error = "Email này chưa được đăng ký.";
                            } else {
                                error += msg;
                            }
                        }
                        callback.onFailure(error);
                    }
                });
    }

}

