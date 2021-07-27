package com.tqd.flexmark.encryptor.internal;

import java.security.MessageDigest;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Charsets;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class ToBeEncrypteNodeRender implements NodeRenderer{
    
	 final public static DataKey<String> SECRET =
			 new DataKey<String>("SECRET", "");
	 private byte [] secret=null; 
	 public ToBeEncrypteNodeRender(DataHolder options) {
		 String s=SECRET.get(options);
		 if(s.length()>0) {
			 secret=s.getBytes(Charsets.UTF_8);
		 }
	 }
	 private int idSequence=0;
	@Override
	public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
		 Set<NodeRenderingHandler<?>> set = new HashSet<>();
	     set.add(new NodeRenderingHandler<>(ToBeEncrypteNode.class, this::render));
	     return set;
	}
    
    void render(ToBeEncrypteNode node, NodeRendererContext context, HtmlWriter html) {
        HtmlRendererOptions htmlOptions = context.getHtmlOptions();
        String text=node.getText().toString();
      
        node.getText();
        if(secret!=null) {
            String cipherText=encrypt(secret,text);
            String id="encrypted_text_id_"+idSequence;
            String onClickValue= "decrypt('"+id+"','"+cipherText+"')";
            StringBuilder sb=new StringBuilder(text.length());
            for(int i=0;i<text.length();i++) {
            	sb.append("*");
            }
            String heihei=sb.toString();
            html.srcPos(BasedSequence.of(heihei)).withAttr()
            .attr("id", id)
            .attr("onClick", onClickValue)
            .attr("class", "encrypted-text")
            .tag("span");
            html.text(heihei);
            idSequence++;
        }else {
        	html.srcPos(BasedSequence.of(text)).withAttr().tag("span");
        	 html.text(text);
        }
        html.tag("/span");
    }

    static {
    	Security.addProvider(new BouncyCastleProvider());
    }
 
    static String CIPHER_ALGORITHM="AES/CBC/PKCS7Padding";
   
    public static String encrypt(byte[] secret,String content){
    	
        try {
        	MessageDigest md=MessageDigest.getInstance("SHA-256");
        	byte[] out=md.digest(secret);
            SecretKey key=new SecretKeySpec(out, "AES");
              //6.根据指定算法AES自成密码器
            Cipher cipher=Cipher.getInstance(CIPHER_ALGORITHM,"BC");
			//7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            byte[] temp = "ABCDEF1234123412".getBytes("UTF-8");
            IvParameterSpec iv = new IvParameterSpec(temp);
            cipher.init(Cipher.ENCRYPT_MODE, key,iv);
            //8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte [] byte_encode=content.getBytes("utf-8");
            //9.根据密码器的初始化方式--加密：将数据加密
            byte [] byte_AES=cipher.doFinal(byte_encode);
            String AES_encode=org.apache.commons.codec.binary.Base64.encodeBase64String(byte_AES);//Base64.getEncoder().encodeToString(byte_AES);
            return AES_encode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;         
    }
 
    	
    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new ToBeEncrypteNodeRender(options);
        }
    }

}
