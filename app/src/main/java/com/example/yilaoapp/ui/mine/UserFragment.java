package com.example.yilaoapp.ui.mine;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.yilaoapp.MainActivity;
import com.example.yilaoapp.R;
import com.example.yilaoapp.bean.Uuid;
import com.example.yilaoapp.bean.messbean;
import com.example.yilaoapp.bean.testbyte;
import com.example.yilaoapp.bean.tok;
import com.example.yilaoapp.databinding.FragmentUserBinding;
import com.example.yilaoapp.service.RetrofitUser;
import com.example.yilaoapp.service.UserService;
import com.example.yilaoapp.service.image_service;
import com.example.yilaoapp.utils.PhotoOperation;
import com.example.yilaoapp.utils.ServiceHelp;
import com.google.gson.Gson;
import com.kongzue.dialog.interfaces.OnMenuItemClickListener;
import com.kongzue.dialog.v3.BottomMenu;
import com.kongzue.dialog.v3.TipDialog;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.yilaoapp.MyApplication.getContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    String mpath = null;

    public UserFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentUserBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        //binding.setData(mineViewModel);
        binding.setLifecycleOwner(requireActivity());
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_chevron_left_24);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller = Navigation.findNavController(v);
                controller.popBackStack();
            }
        });
        binding.userImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomMenu.show((AppCompatActivity) requireContext(), new String[]{"上传图片", "取消"}, new OnMenuItemClickListener() {
                    @Override
                    public void onClick(String text, int index) {
                        //返回参数 text 即菜单名称，index 即菜单索引
                        if (index == 0) {
                            getAuthority();
                        }
                    }
                });
            }
        });
        binding.userXyb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] ba = null;
                if (mpath == null) {
                    Resources res = getResources();
                    Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.touxiang);
                    PhotoOperation Operation = new PhotoOperation();
                    ba = Operation.Bitmap2ByteArray(bmp);
                } else {
                    PhotoOperation Operation = new PhotoOperation();
                    try {
                        ba = Operation.Path2ByteArray(mpath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.d("PhotoFIle", "onClick: 打不开文件");
                    }
                }
                postPortrait(ba);
            }
        });
        return binding.getRoot();
    }

    public void getAuthority(){
        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
        //检测是否有读的权限
        int permission = ContextCompat.checkSelfPermission(requireContext(),
                "android.permission.READ_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有读的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, 1);
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            TipDialog.show((AppCompatActivity) getActivity(), "上传失败", TipDialog.TYPE.ERROR);
        } else {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, 200);//打开系统相册

        }
    }

    public void postPortrait(byte[] ba){
        Map<String, RequestBody> map = new HashMap<>();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/from-data"), ba);
        //注意：file就是与服务器对应的key,后面filename是服务器得到的文件名
        map.put("file\"; filename=\"" + "1.jpeg", requestFile);
        SharedPreferences pre2 = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        String mobile2 = pre2.getString("mobile", "");
        String token2 = pre2.getString("token", "");
        image_service img = new RetrofitUser().get(getContext()).create(image_service.class);
        Call<ResponseBody> image_call = img.send_photo(mobile2, token2, "df3b72a07a0a4fa1854a48b543690eab", map);
        image_call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() / 100 == 4) {
                    Toast.makeText(getContext(),"上传失败，请重新上传",Toast.LENGTH_LONG).show();
                } else if (response.code() / 100 == 5) {
                    Toast.makeText(getContext(), "服务器错误", Toast.LENGTH_SHORT).show();
                } else if (response.code() / 100 == 1 ||
                        response.code() / 100 == 3) {
                    Toast.makeText(getContext(), "13错误", Toast.LENGTH_SHORT).show();
                } else {
                    String uid = "";
                    try {
                        uid = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    response.body().close();
                    Gson gson = new Gson();
                    Uuid u = gson.fromJson(uid, Uuid.class);
                    String name, sex, address;
                    String photo = "";
                    photo = u.getUuid();
                    name = binding.userName.getText().toString();
                    if (binding.userSwitch.isChecked())
                        sex = "female";
                    else
                        sex = "male";
                    address = binding.userSchool.getText().toString();
                    if (name != null && sex != null && address != null) {
                        updateInfo(photo,name,sex,address,u);
                    }
                    Toast.makeText(getContext(), "success", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void updateInfo(String photo,String name,String sex,String address,Uuid u){
        UserService messservice = new RetrofitUser().get(getContext()).create(UserService.class);
        messbean mess = new messbean(photo, name, sex, address);
        SharedPreferences pre = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        String mobile = pre.getString("mobile", "");
        String token = pre.getString("token", "");
        Call<ResponseBody> update = messservice.update(mobile, "df3b72a07a0a4fa1854a48b543690eab", token, mess);
        update.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                SharedPreferences pre = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                SharedPreferences.Editor e = pre.edit();
                e.putString("sex", sex);
                e.putString("id_name",name);
                e.putString("id_school",address);
                e.putString("id_photo",u.getUuid());
                e.apply();
                TipDialog.show((AppCompatActivity) getActivity(), "注册成功", TipDialog.TYPE.SUCCESS);
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull android.os.Message msg) {
                        return false;
                    }
                }).sendEmptyMessageDelayed(0, 3000);Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && null != data) {
            if (Build.VERSION.SDK_INT >= 19) {
                try {
                    handleImageOnKitkat(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    handleImageBeforeKitkat(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) throws FileNotFoundException {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//根据图片路径显示图片
        mpath = imagePath;
        Log.d("123", "handleImageOnKitkat: " + mpath);
    }

    private void handleImageBeforeKitkat(Intent data) throws FileNotFoundException {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);

    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Log.d("PhotoMoment", "getImagePath: " + path);
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) throws FileNotFoundException {
        if (imagePath != null) {
            FileInputStream fis = new FileInputStream(imagePath);
            Bitmap bitmap  = BitmapFactory.decodeStream(fis);
            binding.userImage2.setImageBitmap(bitmap);
            Log.d("UserFagment", "displayImage: " + imagePath);
        } else {
            Toast.makeText(getContext(), "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
}