package com.zhy.market.controller;

import com.zhy.market.domain.Admin;
import com.zhy.market.domain.AdminInfo;
import com.zhy.market.mapper.AdminMapper;
import com.zhy.market.mapper.RsaKeyMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import javax.crypto.Cipher;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequestMapping("admin")
@RestController
public class AdminController {
    private final AdminMapper adminMapper;

    @Resource
    private RsaKeyMapper rsaKeyMapper;

    public AdminController(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @PostMapping("adminLogin")
    public Object adminLogin(@RequestBody Admin userLoginInfo)
            throws Exception {
        String userName = userLoginInfo.userName;
        String passWord = userLoginInfo.passWord;

        List<Admin> list = adminMapper.adminLogin(userName);
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        if (list.isEmpty()) {
            json.put("code", "1");
            json.put("msg", "用户不存在");
            return json;
        }
        Object info = list.get(0);


        String privateKey = rsaKeyMapper.getPrivateKey();
        // 64位解码加密后的字符串
        byte[] sentPassWord = Base64.decodeBase64(passWord.getBytes(StandardCharsets.UTF_8));
        byte[] getPassWord = Base64.decodeBase64(((String) Objects.requireNonNull(getFieldValueByName("passWord", info))).getBytes(StandardCharsets.UTF_8));
        // base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);

        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));

        // RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outSentPassWord = new String(cipher.doFinal(sentPassWord));
        String outGetPassWord = new String(cipher.doFinal(getPassWord));

        List<String> fields = new ArrayList<>();

        fields.add("adminId");
        fields.add("adminRole");
        fields.add("adminUUid");
        fields.add("emailAddress");
        fields.add("phoneNumber");
        fields.add("userName");

        if (outGetPassWord.equals(outSentPassWord)) {
            json.put("code", "0");
            json.put("msg", "登陆成功");
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            for (String x : fields) {
                data.put(x, Objects.requireNonNull(getFieldValueByName(x, info)));
            }
            json.put("data", data);
        } else {
            json.put("code", "1");
            json.put("msg", "密码错误");
        }
        return json;
    }

    private Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method methodG = o.getClass().getMethod(getter);
            return methodG.invoke(o);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("adminRegis")
    public Object adminRegis(HttpServletRequest ignoredRequest, @RequestBody Admin userRegisInfo) {
        String pin = userRegisInfo.pin;
        String userName = userRegisInfo.userName;
        String pinShould = System.getenv("ADMIN_REGISTRATION_PIN");

        if (pinShould == null || pinShould.isBlank() || !Objects.equals(pin, pinShould)) {
            java.util.Map<String, Object> json = new java.util.HashMap<>();
            json.put("code", "1");
            json.put("msg", "pin码错误");
            return json;
        } else if (adminMapper.checkUserName(userName) == 1) {
            java.util.Map<String, Object> json = new java.util.HashMap<>();
            json.put("code", "1");
            json.put("msg", "用户名已存在");
            return json;
        } else {
            String passWord = userRegisInfo.passWord;
            String emailAddress = userRegisInfo.emailAddress;
            String phoneNumber = userRegisInfo.phoneNumber;
            UUID uuid = UUID.randomUUID();
            int result = adminMapper.adminRegis(userName, passWord, emailAddress, phoneNumber, uuid.toString(), 1);
            java.util.Map<String, Object> json = new java.util.HashMap<>();
            if (result == 1) {
                json.put("code", "0");
                json.put("msg", "注册成功");
            } else {
                json.put("code", "1");
                json.put("msg", "注册失败");
            }
            return json;
        }
    }

    @GetMapping("getAllAdminsInfo")
    public Object getAllAdminsInfo(HttpServletRequest request) {
        int role = Integer.parseInt(request.getParameter("adminRole"));

        List<AdminInfo> adminList = adminMapper.getAllAdminsInfo();
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        if (role == 0) {
            json.put("code", "0");
            json.put("data", adminList);
        }

        return json;
    }

    @GetMapping("deleteAdmin")
    public Object deleteAdmin(HttpServletRequest request) {
        String adminUUid = request.getParameter("adminUUid");

        int deleteCount = adminMapper.deleteAdmin(adminUUid);

        java.util.Map<String, Object> json = new java.util.HashMap<>();
        if (deleteCount == 1) {
            json.put("code", "0");
            json.put("data", deleteCount);
            json.put("msg", "删除成功");
        } else {
            json.put("code", "1");
            json.put("data", deleteCount);
            json.put("msg", "删除失败");
        }
        return json;
    }
}
