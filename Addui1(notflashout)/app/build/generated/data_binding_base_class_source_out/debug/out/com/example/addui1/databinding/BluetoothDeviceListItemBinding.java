// Generated by view binder compiler. Do not edit!
package com.example.addui1.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.addui1.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class BluetoothDeviceListItemBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView deviceInfo;

  @NonNull
  public final TextView deviceName;

  private BluetoothDeviceListItemBinding(@NonNull LinearLayout rootView,
      @NonNull TextView deviceInfo, @NonNull TextView deviceName) {
    this.rootView = rootView;
    this.deviceInfo = deviceInfo;
    this.deviceName = deviceName;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static BluetoothDeviceListItemBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static BluetoothDeviceListItemBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.bluetooth_device_list_item, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static BluetoothDeviceListItemBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.device_info;
      TextView deviceInfo = ViewBindings.findChildViewById(rootView, id);
      if (deviceInfo == null) {
        break missingId;
      }

      id = R.id.device_name;
      TextView deviceName = ViewBindings.findChildViewById(rootView, id);
      if (deviceName == null) {
        break missingId;
      }

      return new BluetoothDeviceListItemBinding((LinearLayout) rootView, deviceInfo, deviceName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
