package com.na.android.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.na.android.utils.easypermissions.AfterPermissionGranted;
import com.na.android.utils.easypermissions.EasyPermissions;

import java.util.List;

/**
 * @actor:taotao
 * @DATE: 17/2/21
 */

public class NaPermissionUtils {


    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param perms   one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String... perms) {
        return EasyPermissions.hasPermissions(context, perms);
    }

    /**
     * Request a set of permissions, showing a rationale if the system requests it.
     *
     * @see #requestPermissions(Activity, String, int, int, int, String...)
     */
    public static void requestPermissions(@NonNull Activity activity,
                                          @NonNull String rationale,
                                          int requestCode,
                                          @NonNull String... perms) {
        EasyPermissions.requestPermissions(
                activity,
                rationale,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param activity       {@link Activity} requesting permissions. Should implement {@link
     *                       ActivityCompat.OnRequestPermissionsResultCallback} or override {@link
     *                       FragmentActivity#onRequestPermissionsResult(int, String[], int[])} if
     *                       it extends from {@link FragmentActivity}.
     * @param rationale      a message explaining why the application needs this set of permissions,
     *                       will be displayed if the user rejects the request the first time.
     * @param positiveButton custom text for positive button
     * @param negativeButton custom text for negative button
     * @param requestCode    request code to track this request, must be < 256.
     * @param perms          a set of permissions to be requested.
     * @see Manifest.permission
     */
    @SuppressLint("NewApi")
    public static void requestPermissions(@NonNull Activity activity,
                                          @NonNull String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          int requestCode,
                                          @NonNull String... perms) {
        EasyPermissions.requestPermissions(
                activity,
                rationale,
                positiveButton,
                negativeButton,
                requestCode, perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @see #requestPermissions(Fragment, String, int, int, int, String...)
     */
    public static void requestPermissions(@NonNull Fragment fragment,
                                          @NonNull String rationale,
                                          int requestCode,
                                          @NonNull String... perms) {
        EasyPermissions.requestPermissions(
                fragment,
                rationale,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param fragment {@link Fragment} requesting permissions. Should override {@link
     *                 Fragment#onRequestPermissionsResult(int, String[], int[])}.
     * @see #requestPermissions(Activity, String, int, int, int, String...)
     */
    @SuppressLint("NewApi")
    public static void requestPermissions(@NonNull Fragment fragment,
                                          @NonNull String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          int requestCode,
                                          @NonNull String... perms) {
        EasyPermissions.requestPermissions(
                fragment,
                rationale,
                positiveButton,
                negativeButton,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @see #requestPermissions(android.app.Fragment, String, int, int, int, String...)
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void requestPermissions(@NonNull android.app.Fragment fragment,
                                          @NonNull String rationale,
                                          int requestCode,
                                          @NonNull String... perms) {
        EasyPermissions.requestPermissions(
                fragment,
                rationale,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param fragment {@link android.app.Fragment} requesting permissions. Should override {@link
     *                 android.app.Fragment#onRequestPermissionsResult(int, String[], int[])}.
     * @see #requestPermissions(Activity, String, int, int, int, String...)
     */
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void requestPermissions(@NonNull android.app.Fragment fragment,
                                          @NonNull String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          int requestCode,
                                          @NonNull String... perms) {
        EasyPermissions.requestPermissions(
                fragment,
                rationale,
                positiveButton,
                negativeButton,
                requestCode, perms);
    }

    /**
     * Handle the result of a permission request, should be called from the calling {@link
     * Activity}'s {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
     * String[], int[])} method.
     * <p>
     * If any permissions were granted or denied, the {@code object} will receive the appropriate
     * callbacks through {@link PermissionCallbacks} and methods annotated with {@link
     * AfterPermissionGranted} will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that have a method annotated with {@link
     *                     AfterPermissionGranted} or implement {@link PermissionCallbacks}.
     */
    public static void onRequestPermissionsResult(int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults,
                                                  @NonNull Object... receivers) {
        EasyPermissions.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
                receivers);
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @param activity          {@link Activity} requesting permissions.
     * @param deniedPermissions list of denied permissions, usually from {@link
     *                          PermissionCallbacks#onPermissionsDenied(int, List)}
     * @return {@code true} if at least one permission in the list was permanently denied.
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Activity activity,
                                                          @NonNull List<String> deniedPermissions) {
        return EasyPermissions.somePermissionPermanentlyDenied(activity, deniedPermissions);
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @see #somePermissionPermanentlyDenied(Activity, List)
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Fragment fragment,
                                                          @NonNull List<String> deniedPermissions) {
        return EasyPermissions.somePermissionPermanentlyDenied(fragment, deniedPermissions);
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @see #somePermissionPermanentlyDenied(Activity, List)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean somePermissionPermanentlyDenied(@NonNull android.app.Fragment fragment,
                                                          @NonNull List<String> deniedPermissions) {
        return EasyPermissions.somePermissionPermanentlyDenied(fragment, deniedPermissions);
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @param activity         {@link Activity} requesting permissions.
     * @param deniedPermission denied permission.
     * @return {@code true} if the permissions has been permanently denied.
     */
    public static boolean permissionPermanentlyDenied(@NonNull Activity activity,
                                                      @NonNull String deniedPermission) {
        return EasyPermissions.permissionPermanentlyDenied(activity, deniedPermission);
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @see #permissionPermanentlyDenied(Activity, String)
     */
    public static boolean permissionPermanentlyDenied(@NonNull Fragment fragment,
                                                      @NonNull String deniedPermission) {
        return EasyPermissions.permissionPermanentlyDenied(fragment, deniedPermission);
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @see #permissionPermanentlyDenied(Activity, String)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean permissionPermanentlyDenied(@NonNull android.app.Fragment fragment,
                                                      @NonNull String deniedPermission) {
        return EasyPermissions.permissionPermanentlyDenied(fragment, deniedPermission);
    }

    public interface NaPermissionCallbacks extends EasyPermissions.PermissionCallbacks {

    }

}
