package com.example.yilaoapp.ui.mine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.yilaoapp.R;
import com.example.yilaoapp.bean.Password;
import com.example.yilaoapp.bean.Verify;
import com.example.yilaoapp.databinding.FragmentSigninBinding;
import com.example.yilaoapp.service.RetrofitUser;
import com.example.yilaoapp.service.UserService;
import com.example.yilaoapp.service.Verify_service;
import com.example.yilaoapp.utils.ConfigUtil;
import com.example.yilaoapp.utils.ServiceHelp;
import com.kongzue.dialog.v3.TipDialog;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment {
    public SigninFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    FragmentSigninBinding binding;
    //验证码的倒计时 new倒计时对象,总共的时间,每隔多少秒更新一次时间
    final MyCountDownTimer myCountDownTimer = new MyCountDownTimer(60000,1000);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_signin,container,false);
        //binding.setData(mineViewModel);
        ((ViewDataBinding) binding).setLifecycleOwner(requireActivity());
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_chevron_left_24);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller = Navigation.findNavController(v);
                controller.popBackStack();
            }
        });
        binding.getYZM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone=binding.signinPhone.getText().toString();
                if(!ConfigUtil.isPhoneNum(phone))
                    Toast.makeText(getContext(),"请输入正确的手机号码",Toast.LENGTH_LONG).show();
                else{
                    GetCode(phone);
                }
            }
        });
        binding.signinXyb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd1,pwd2,code,phone;
                pwd1=binding.signinPass.getText().toString();
                pwd2=binding.signinPassagain.getText().toString();
                code=binding.signinInputyzm.getText().toString();
                phone=binding.signinPhone.getText().toString();
               if(pwd1.equals(pwd2)&&ConfigUtil.isPhoneNum(phone)&&code.length()==4){
                    Password pass=new Password(pwd1);
                    Signin(phone,code,pass,v);
                }
               else{
                   Toast.makeText(getContext(),"请输入正确密码或验证码",Toast.LENGTH_LONG).show();
               }
            }
        });

        return binding.getRoot();
        //return inflater.inflate(R.layout.fragment_signin, container, false);
    }

    //倒计时的类和函数
    private class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //计时过程
        @SuppressLint("SetTextI18n")
        @Override
        public void onTick(long l) {
            //防止计时过程中重复点击
            binding.getYZM.setClickable(false);
            binding.getYZM.setText(l/1000+"秒");

        }

        //计时完毕的方法
        @Override
        public void onFinish() {
            //重新给Button设置文字
            binding.getYZM.setText("重新获取");
            //设置可点击
            binding.getYZM.setClickable(true);
        }
    }

    public void GetCode(String phone){
        Verify_service yzmservice=new RetrofitUser().get(getContext()).create(Verify_service.class);
        Verify yz=new Verify("df3b72a07a0a4fa1854a48b543690eab",phone,"PUT","/v1.0/users/"+phone);
        Call<ResponseBody> callback=yzmservice.send_code(yz);
        callback.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() / 100 == 4) {
                    Toast.makeText(getContext(), "4失败", Toast.LENGTH_SHORT).show();
                } else if (response.code() / 100 == 5) {
                    Toast.makeText(getContext(), "服务器错误", Toast.LENGTH_SHORT).show();
                } else if (response.code() / 100 == 1 ||
                        response.code() / 100 == 3) {
                    Toast.makeText(getContext(), "13错误", Toast.LENGTH_SHORT).show();
                } else {
                    TipDialog.show((AppCompatActivity) getActivity(), "发送成功", TipDialog.TYPE.SUCCESS);
                    myCountDownTimer.start();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                TipDialog.show((AppCompatActivity) getActivity(), "发送失败", TipDialog.TYPE.ERROR);
                binding.getYZM.setText("重新获取");
            }
        });
    }

    public void Signin(String phone,String code,Password pass,View v){
        UserService xybservice=new RetrofitUser().get(getContext()).create(UserService.class);
        Call<ResponseBody> xybback=xybservice.sigin(phone,"df3b72a07a0a4fa1854a48b543690eab",code,pass);
        xybback.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                NavController controller = Navigation.findNavController(v);
                controller.popBackStack();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.toString());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

}