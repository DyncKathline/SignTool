package com.xiaoying.signtool;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtInput;
    private TextView mTvMsg;
    private TextView mTvKeyHash;
    private TextView mTvMD5;
    private TextView mTvSHA1;
    private TextView mTvSHA256;

    private String mLineSeparator = System.getProperty("line.separator", "\n");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtInput = findViewById(R.id.et_input);
        mTvMsg = findViewById(R.id.tv_msg);
        mTvKeyHash = findViewById(R.id.tv_key_hash);
        mTvMD5 = findViewById(R.id.tv_md5_fingerprint);
        mTvSHA1 = findViewById(R.id.tv_sha1_fingerprint);
        mTvSHA256 = findViewById(R.id.tv_sha256_fingerprint);

        rippleView(mTvKeyHash);
        rippleView(mTvMD5);
        rippleView(mTvSHA1);
        rippleView(mTvSHA256);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_signature_key_hash:
                getSignatureInfo();
                break;
            case R.id.tv_key_hash:
            case R.id.tv_md5_fingerprint:
            case R.id.tv_sha1_fingerprint:
            case R.id.tv_sha256_fingerprint:
                if(v instanceof TextView) {
                    copyToClipboard(((TextView) v).getText().toString());
                }
            default:
                break;
        }
    }

    /**
     * 获取签名信息
     */
    private void getSignatureInfo() {
        final String packageName = mEtInput.getText().toString().trim();
        if(TextUtils.isEmpty(packageName)) {
            mTvMsg.setText(R.string.package_name_hint);
            return;
        }
        mTvMsg.setText(String.format(getResources().getString(R.string.package_name_format), packageName));
        mTvMsg.append(mLineSeparator);
        mTvMsg.append(mLineSeparator);
        mTvMsg.append(getResources().getString(R.string.tips_click_to_copy_to_clipboard));

        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            String keyHash;
            String md5FingerPrint;
            String sha1FingerPrint;
            String sha256FingerPrint;
            MessageDigest md;
            for (Signature signature : info.signatures) {
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("KeyHash:", keyHash);
                mTvKeyHash.setText(keyHash);

                md = MessageDigest.getInstance("MD5");
                md.update(signature.toByteArray());
                md5FingerPrint = convert2HexFormatted(md.digest());
                Log.e("MD5:", md5FingerPrint);
                mTvMD5.setText(md5FingerPrint);

                md = MessageDigest.getInstance("SHA-1");
                md.update(signature.toByteArray());
                sha1FingerPrint = convert2HexFormatted(md.digest());
                Log.e("SHA-1:", sha1FingerPrint);
                mTvSHA1.setText(sha1FingerPrint);

                md = MessageDigest.getInstance("SHA-256");
                md.update(signature.toByteArray());
                sha256FingerPrint = convert2HexFormatted(md.digest());
                Log.e("SHA-256:", sha256FingerPrint);
                mTvSHA256.setText(sha256FingerPrint);

            }
        } catch (PackageManager.NameNotFoundException e) {
            // do nothing
            e.printStackTrace();
            mTvMsg.append(getResources().getString(R.string.error_package_not_found));
        } catch (NoSuchAlgorithmException e) {
            // do nothing
            e.printStackTrace();
            mTvMsg.setText(getResources().getString(R.string.error_get_signature_failed));
        }

    }


    /**
     * 将签名信息的byte数组转换成16进制字符串
     * @param array 签名byte数组
     * @return 十六进制字符串，每两位用":"分隔开
     */
    private static String convert2HexFormatted(byte[] array) {
        if(null == array) {
            return null;
        }

        StringBuilder str = new StringBuilder();
        String hex;
        int hexLength;
        for(byte b : array) {
            hex = Integer.toHexString(b);
            hexLength = hex.length();
            if(hexLength < 2) {
                // 位数小于2，左边加0补足成两位
                hex = "0" + hex;
            }
            if(hexLength > 2) {
                // 位数大于2，截取最后两位
                hex = hex.substring(hexLength - 2, hexLength);
            }
            str.append(hex.toUpperCase())
                    .append(":"); // 添加":"分隔符
        }
        str.replace(str.length() - 1, str.length(), ""); // 去掉最后一个多余的":"分割符
        return str.toString();
    }

    /**
     * 复制到剪切板
     * @param content 复制的内容
     */
    private void copyToClipboard(CharSequence content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(null == cm) {
            Toast.makeText(MainActivity.this, R.string.fail_to_copy_to_clipboard, Toast.LENGTH_SHORT).show();
            return;
        }
        // 将文本内容放到系统剪贴板里。
        cm.setPrimaryClip(ClipData.newPlainText("text", content));
        Toast.makeText(MainActivity.this, R.string.already_copy_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void rippleView(View view) {
        MaterialRippleLayout.on(view)
                .rippleOverlay(true)
                .rippleHover(true)
                .rippleAlpha(0.2f)
                .create();
    }
}