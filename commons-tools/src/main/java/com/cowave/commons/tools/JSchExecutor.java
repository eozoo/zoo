/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @decs
 * @auther cailinfei
 * @date 2023/12/20 9:15
 */
public class JSchExecutor {
    private static Logger log = LoggerFactory.getLogger(JSchExecutor.class);

    private String charset = "UTF-8"; // 设置编码格式
    private String user; // 用户名
    private String passwd; // 登录密码
    private String host; // 主机IP
    private int port = 22; //默认端口
    private JSch jsch;
    private Session session;

    private ChannelSftp sftp;

    /**
     * @param user   用户名
     * @param passwd 密码
     * @param host   主机IP
     */
    public JSchExecutor(String user, String passwd, String host) {
        this.user = user;
        this.passwd = passwd;
        this.host = host;
    }

    /**
     * @param user   用户名
     * @param passwd 密码
     * @param host   主机IP
     */
    public JSchExecutor(String user, String passwd, String host, int port) {
        this.user = user;
        this.passwd = passwd;
        this.host = host;
        this.port = port;
    }

    /**
     * 连接到指定的IP
     *
     * @throws JSchException
     */
    public void connect() throws JSchException {
        jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(passwd);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        sftp = (ChannelSftp) channel;
        log.info("连接到SFTP成功。host: " + host);
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * 执行一条命令
     */
    public int execCmd(String command) throws Exception {
        log.info("开始执行命令:" + command);
        int returnCode = -1;
        BufferedReader reader = null;
        Channel channel = null;

        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        reader = new BufferedReader(new InputStreamReader(in));//中文乱码貌似这里不能控制，看连接的服务器的

        channel.connect();
        String buf;
        while ((buf = reader.readLine()) != null) {
            log.info(buf);
        }
        reader.close();
        // Get the return code only after the channel is closed.
        if (channel.isClosed()) {
            returnCode = channel.getExitStatus();
        }
        log.info("Exit-status:" + returnCode);
        channel.disconnect();
        return returnCode;
    }

    /**
     * 执行相关的命令
     */
    public void execCmd() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String command = "";
        BufferedReader reader = null;
        Channel channel = null;

        try {
            while ((command = br.readLine()) != null) {
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                channel.connect();
                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in,
                        Charset.forName(charset)));
                String buf = null;
                while ((buf = reader.readLine()) != null) {
                    System.out.println(buf);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel.disconnect();
        }
    }

    /**
     * 上传文件
     */
    public void uploadFile(InputStream inputStream, String remote) throws Exception {
        try {
            String rpath = remote.substring(0, remote.lastIndexOf("/") + 1);
            if (!isDirExist(rpath)) {
                createDir(rpath);
            }
            sftp.setInputStream(inputStream);
            sftp.put(inputStream, remote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件
     */
    public void downloadFile(String remote, String local) throws Exception {
        OutputStream outputStream = null;
        try {
            sftp.connect(5000);
            outputStream = new FileOutputStream(new File(local));
            sftp.get(remote, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * 移动到相应的目录下
     *
     * @param pathName 要移动的目录
     * @return
     */
    public boolean changeDir(String pathName) {
        if (pathName == null || pathName.trim().equals("")) {
            log.debug("invalid pathName");
            return false;
        }
        try {
            sftp.cd(pathName.replaceAll("\\\\", "/"));
            log.debug("directory successfully changed,current dir=" + sftp.pwd());
            return true;
        } catch (SftpException e) {
            log.error("failed to change directory", e);
            return false;
        }
    }

    /**
     * 创建一个文件目录，mkdir每次只能创建一个文件目录
     * 或者可以使用命令mkdir -p 来创建多个文件目录
     */
    public void createDir(String createpath) {
        try {
            if (isDirExist(createpath)) {
                sftp.cd(createpath);
                return;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if ("".equals(path)) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }
            }
            sftp.cd(createpath);
        } catch (SftpException e) {
            throw new RuntimeException("创建路径错误：" + createpath);
        }
    }


    /**
     * 判断目录是否存在
     *
     * @param directory
     * @return
     */
    public boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpAttrs = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpAttrs.isDir();
        } catch (Exception e) {
            if (e.getMessage().equalsIgnoreCase("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }


}
