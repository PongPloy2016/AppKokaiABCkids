package com.pongploydev.kokaiabckids.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;


public class AppUpdateCustomsManager {
    private static final String TAG = "AppUpdateManager";
    private static final int REQUEST_CODE_UPDATE = 1001;

    private Activity activity;
    private com.google.android.play.core.appupdate.AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    // Constants for update types
    public static final int FLEXIBLE_UPDATE = AppUpdateType.FLEXIBLE;
    public static final int IMMEDIATE_UPDATE = AppUpdateType.IMMEDIATE;

    private boolean isCheckingForUpdate = false;
    private OnUpdateCheckListener updateCheckListener;

    public interface OnUpdateCheckListener {
        void onUpdateAvailable(AppUpdateInfo updateInfo);
        void onUpdateNotAvailable();
        void onUpdateCheckFailed(String error);
    }

    public AppUpdateCustomsManager(Activity activity) {
        this.activity = activity;
        initialize();
    }

    private void initialize() {
        try {
            // Initialize App Update Manager
            appUpdateManager = AppUpdateManagerFactory.create(activity);

            // Create install state listener
            installStateUpdatedListener = new InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    handleInstallStateUpdate(state);
                }
            };

            // Register listener
            appUpdateManager.registerListener(installStateUpdatedListener);

            Log.d(TAG, "AppUpdateManager initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing AppUpdateManager", e);
        }
    }

    /**
     * ตรวจสอบว่ามีอัพเดตใหม่หรือไม่
     */
    public void checkForUpdate(int updateType, OnUpdateCheckListener listener) {
        if (isCheckingForUpdate) {
            Log.w(TAG, "Update check already in progress");
            return;
        }

        this.updateCheckListener = listener;
        isCheckingForUpdate = true;

        try {
            // Returns a task that asynchronously provides AppUpdateInfo
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    isCheckingForUpdate = false;
                    handleUpdateInfo(appUpdateInfo, updateType);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    isCheckingForUpdate = false;
                    Log.e(TAG, "Failed to check for update", e);
                    if (updateCheckListener != null) {
                        updateCheckListener.onUpdateCheckFailed(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            isCheckingForUpdate = false;
            Log.e(TAG, "Exception while checking for update", e);
            if (updateCheckListener != null) {
                updateCheckListener.onUpdateCheckFailed(e.getMessage());
            }
        }
    }

    /**
     * จัดการข้อมูลอัพเดตที่ได้รับ
     */
    private void handleUpdateInfo(AppUpdateInfo appUpdateInfo, int updateType) {
        try {
            Log.d(TAG, "Update availability: " + appUpdateInfo.updateAvailability());
            Log.d(TAG, "Update priority: " + appUpdateInfo.updatePriority());
            Log.d(TAG, "Is update type allowed: " + appUpdateInfo.isUpdateTypeAllowed(updateType));

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(updateType)) {

                if (updateCheckListener != null) {
                    updateCheckListener.onUpdateAvailable(appUpdateInfo);
                }

                // แสดง dialog แจ้งผู้ใช้เกี่ยวกับอัพเดต
                showUpdateAvailableDialog(appUpdateInfo, updateType);

            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // หากมีอัพเดตที่กำลังดำเนินการอยู่ ให้ดำเนินการต่อ
                if (updateType == IMMEDIATE_UPDATE) {
                    startUpdate(appUpdateInfo, updateType);
                }
            } else {
                if (updateCheckListener != null) {
                    updateCheckListener.onUpdateNotAvailable();
                }
                Log.d(TAG, "No update available or update type not allowed");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling update info", e);
            if (updateCheckListener != null) {
                updateCheckListener.onUpdateCheckFailed(e.getMessage());
            }
        }
    }

    /**
     * แสดง dialog แจ้งว่ามีอัพเดตใหม่
     */
    private void showUpdateAvailableDialog(final AppUpdateInfo appUpdateInfo, final int updateType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("อัพเดตใหม่มีให้ดาวน์โหลด")
                .setMessage("มีเวอร์ชันใหม่ของแอปพลิเคชัน คุณต้องการอัพเดตตอนนี้หรือไม่?")
                .setPositiveButton("อัพเดตตอนนี้", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUpdate(appUpdateInfo, updateType);
                    }
                })
                .setNegativeButton("ภายหลัง", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ผู้ใช้เลือกอัพเดตภายหลัง
                        Toast.makeText(activity, "คุณสามารถอัพเดตได้ภายหลังจากการตั้งค่า", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * เริ่มกระบวนการอัพเดต
     */
    public void startUpdate(AppUpdateInfo appUpdateInfo, int updateType) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateType,
                    activity,
                    REQUEST_CODE_UPDATE);

            Log.d(TAG, "Update flow started for type: " + updateType);

        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Error starting update flow", e);
            Toast.makeText(activity, "เกิดข้อผิดพลาดในการเริ่มต้นอัพเดต", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * จัดการสถานะการติดตั้ง
     */
    private void handleInstallStateUpdate(InstallState state) {
        Log.d(TAG, "Install state updated: " + state.installStatus());

        switch (state.installStatus()) {
            case InstallStatus.DOWNLOADING:
                // กำลังดาวน์โหลด
                long bytesDownloaded = state.bytesDownloaded();
                long totalBytesToDownload = state.totalBytesToDownload();
                int progress = (int) ((bytesDownloaded * 100) / totalBytesToDownload);
                Log.d(TAG, "Download progress: " + progress + "%");
                break;

            case InstallStatus.DOWNLOADED:
                // ดาวน์โหลดเสร็จสิ้น - สำหรับ Flexible Update
                showCompletionDialog();
                break;

            case InstallStatus.INSTALLED:
                // ติดตั้งเสร็จสิ้น
                Log.d(TAG, "Update installed successfully");
                if (appUpdateManager != null) {
                    appUpdateManager.unregisterListener(installStateUpdatedListener);
                }
                break;

            case InstallStatus.FAILED:
                // การติดตั้งล้มเหลว
                Log.e(TAG, "Update installation failed");
                Toast.makeText(activity, "การอัพเดตล้มเหลว กรุณาลองอีกครั้ง", Toast.LENGTH_SHORT).show();
                break;

            case InstallStatus.CANCELED:
                // ผู้ใช้ยกเลิกการอัพเดต
                Log.d(TAG, "Update was canceled by user");
                break;
        }
    }

    /**
     * แสดง dialog เมื่อดาวน์โหลดเสร็จ (สำหรับ Flexible Update)
     */
    private void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("ดาวน์โหลดเสร็จสิ้น")
                .setMessage("อัพเดตดาวน์โหลดเสร็จเรียบร้อยแล้ว คุณต้องการติดตั้งตอนนี้หรือไม่?")
                .setPositiveButton("ติดตั้ง", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        completeUpdate();
                    }
                })
                .setNegativeButton("ภายหลัง", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, "คุณสามารถติดตั้งได้ภายหลัง", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * ติดตั้งอัพเดต (สำหรับ Flexible Update)
     */
    public void completeUpdate() {
        if (appUpdateManager != null) {
            appUpdateManager.completeUpdate();
        }
    }

    /**
     * ตรวจสอบว่ามีอัพเดตที่กำลังดำเนินการอยู่หรือไม่
     */
    public void checkForUpdateInProgress() {
        try {
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // ดำเนินการอัพเดตต่อ
                        startUpdate(appUpdateInfo, IMMEDIATE_UPDATE);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error checking for update in progress", e);
        }
    }

    /**
     * จัดการผลลัพธ์จากอัพเดต
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.w(TAG, "Update flow failed! Result code: " + resultCode);

                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(activity, "อัพเดตถูกยกเลิก", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_FIRST_USER:
                        Toast.makeText(activity, "ผู้ใช้ปฏิเสธการอัพเดต", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(activity, "การอัพเดตล้มเหลว", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            } else {
                Log.d(TAG, "Update flow completed successfully");
                return true;
            }
        }
        return false;
    }

    /**
     * ตรวจสอบอัพเดตแบบเงียบ (Silent Update Check)
     */
    public void checkForSilentUpdate() {
        checkForUpdate(FLEXIBLE_UPDATE, new OnUpdateCheckListener() {
            @Override
            public void onUpdateAvailable(AppUpdateInfo updateInfo) {
                // สำหรับอัพเดตแบบเงียบ อาจจะไม่แสดง dialog
                Log.d(TAG, "Silent update available");
                // สามารถเริ่มดาวน์โหลดในพื้นหลังได้ที่นี่
            }

            @Override
            public void onUpdateNotAvailable() {
                Log.d(TAG, "No silent update available");
            }

            @Override
            public void onUpdateCheckFailed(String error) {
                Log.e(TAG, "Silent update check failed: " + error);
            }
        });
    }

    /**
     * ตั้งค่าการแจ้งเตือนอัพเดต
     */
    public void setUpdateNotificationEnabled(boolean enabled) {
        // สามารถบันทึกการตั้งค่าใน SharedPreferences ได้ที่นี่
    }

    /**
     * ล้างทรัพยากร
     */
    public void cleanup() {
        try {
            if (appUpdateManager != null && installStateUpdatedListener != null) {
                appUpdateManager.unregisterListener(installStateUpdatedListener);
            }
            Log.d(TAG, "AppUpdateManager cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }

    /**
     * ตรวจสอบว่ามีการอัพเดตที่จำเป็นต้องรีสตาร์ทหรือไม่
     */
    public boolean requiresRestart() {
        // สำหรับ Immediate updates ที่ต้องการรีสตาร์ทแอป
        return false;
    }

    /**
     * ดึงข้อมูลเวอร์ชันปัจจุบันและเวอร์ชันใหม่
     */
    public void getVersionInfo(OnVersionInfoListener listener) {
        try {
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (listener != null) {
                        listener.onVersionInfoAvailable(
                                appUpdateInfo.availableVersionCode(),
                                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        );
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error getting version info", e);
        }
    }

    public interface OnVersionInfoListener {
        void onVersionInfoAvailable(long availableVersionCode, boolean updateAvailable);
    }
}