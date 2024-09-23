/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script;

import cn.hutool.core.util.CharsetUtil;
import com.cowave.script.local.BaseLocalScriptOutputStream;
import com.jcraft.jsch.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author jiangbo
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScriptUtils {

    /**
     * 执行命令行
     *
     * @param cmd linux 命令行
     * @return 是否执行成功
     * @throws IOException          异常
     * @throws InterruptedException 中断
     */
    public static boolean execCmd(String cmd) throws IOException, InterruptedException {
        String[] command = {"/bin/bash", "-c", cmd};
        return execCmd(command);
    }

    /**
     * 执行命令行
     *
     * @param exec linux 命令行
     * @return 是否执行成功
     * @throws IOException          异常
     * @throws InterruptedException 中断
     */
    public static boolean execCmd(String[] exec) throws IOException, InterruptedException {
        return execCmd(exec, null, null);
    }

    /**
     * 执行命令行
     *
     * @param exec    linux 命令行
     * @param workDir 命令执行路径
     * @return 是否执行成功
     * @throws IOException          异常
     * @throws InterruptedException 中断
     */
    public static boolean execCmd(String[] exec, Map<String, String> env, String workDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(exec);

        if (workDir != null) {
            pb.directory(new File(workDir));
        }
        if (MapUtils.isNotEmpty(env)) {
            Map<String, String> pbEnv = pb.environment();
            pbEnv.putAll(env);
        }
        Process process = pb.start();
        int success = process.waitFor();
        return success == 0;
    }

    /**
     * 执行命令行
     *
     * @param cmd linux 命令行
     * @return 执行结果
     */
    public static Result exec(String cmd) {
        String[] command = {"/bin/bash", "-c", cmd};
        return runProcess(command, null);
    }

    public static Result exec(String cmd, BaseLocalScriptOutputStream localScriptOutputStream) {
        String[] command = {"/bin/bash", "-c", cmd};
        return runProcess(command, null, localScriptOutputStream);
    }

    public static Result execByWindows(String cmd) {
        String[] command = {"cmd.exe", "/c", cmd};
        return runProcess(command, null);
    }

    public static Result execByWindows(String cmd, BaseLocalScriptOutputStream localScriptOutputStream) {
        String[] command = {"cmd.exe", "/c", cmd};
        return runProcess(command, null, localScriptOutputStream);
    }

    public static Result runProcess(String[] exec, Map<String, String> env) {
        ProcessBuilder pb = new ProcessBuilder(exec);
        if (MapUtils.isNotEmpty(env)) {
            Map<String, String> pbEnv = pb.environment();
            pbEnv.putAll(env);
        }
        try {
            Process p = pb.start();
            String charset = getCharset(exec);
            String output = IOUtils.toString(p.getInputStream(), charset);
            String error = IOUtils.toString(p.getErrorStream(), charset);
            int exitCode = p.waitFor();
            return new Result(exitCode == 0, exitCode, output, error);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("{}", Thread.interrupted(), e);
            return new Result(false, -99, null, e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            return new Result(false, -99, null, e.getMessage());
        }
    }

    public static Result runProcess(String[] exec, Map<String, String> env, BaseLocalScriptOutputStream baseLocalScriptOutputStream) {
        ProcessBuilder pb = new ProcessBuilder(exec);
        if (MapUtils.isNotEmpty(env)) {
            Map<String, String> pbEnv = pb.environment();
            pbEnv.putAll(env);
        }
        try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String charset = getCharset(exec);
            InputStream inputStream = p.getInputStream();
            baseLocalScriptOutputStream.wrap(inputStream, charset);
            int exitCode = p.waitFor();
            return new Result(exitCode == 0, exitCode, null, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("{}", Thread.interrupted(), e);
            return new Result(false, -99, null, e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            return new Result(false, -99, null, e.getMessage());
        } finally {
            IOUtils.closeQuietly(baseLocalScriptOutputStream);
        }

    }

    private static String getCharset(String[] exec) {
        if (exec == null || exec.length == 0) {
            return CharsetUtil.UTF_8;
        }
        if ("cmd.exe".equals(exec[0])) {
            return CharsetUtil.GBK;
        }
        return CharsetUtil.UTF_8;
    }

    /**
     * 执行远程命令
     */
    public static Result exec(String ip, Integer port, String username, String password, String command) {
        return exec(ip, port, username, password, command, 0, CharsetUtil.UTF_8);
    }

    /**
     * 执行远程命令
     */
    public static Result exec(String ip, Integer port, String username, String password, String command, Integer timeout) {
        return exec(ip, port, username, password, command, timeout, CharsetUtil.UTF_8);
    }

    /**
     * 执行远程命令
     * windows 使用gbk编码
     */
    public static Result exec(String ip, Integer port, String username, String password, String command, Integer timeout, String charsetName) {
        try(DefaultScriptOutputStream outputStream = new DefaultScriptOutputStream(Charset.forName(charsetName));
            DefaultScriptOutputStream extOutputStream = new DefaultScriptOutputStream(Charset.forName(charsetName))) {
            return exec(ip, port, username, password, command, timeout, outputStream, extOutputStream);
        } catch (Exception e) {
            log.error("", e);
            return new Result(false, -99, null, e.getMessage());
        }
    }

    public static Result exec(String ip, Integer port, String username, String password, String command,
                              com.cowave.script.BaseScriptOutputStream outputStream, com.cowave.script.BaseScriptOutputStream extOutputStream) {
        return exec(ip, port, username, password, command, 0, outputStream, extOutputStream);
    }

    public static Result exec(String ip, Integer port, String username, String password, String command,
                              Integer timeout, com.cowave.script.BaseScriptOutputStream outputStream, com.cowave.script.BaseScriptOutputStream extOutputStream) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = createSession(jsch, ip, port, username, password, timeout);
            return channelExec(session, command, timeout, outputStream, extOutputStream);
        } catch (Exception e) {
            log.error("", e);
            return new Result(false, -99, null, e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 拷贝文件到目标服务器
     *
     * @param remotePathFile 目标服务器文件路径（包括文件名）
     */
    public static void copyFile(InputStream inputStream, String ip, Integer port, String username, String password, String remotePathFile) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = createSession(jsch, ip, port, username, password, 0);
            channelSftp(session, inputStream, remotePathFile);
        } catch (Exception e) {
            throw new UnsupportedOperationException("复制文件失败", e);
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private static Session createSession(JSch jsch, String ip, Integer port, String username, String password, int timeout) throws JSchException {
        Session session = jsch.getSession(username, ip, port);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        session.setTimeout(timeout);
        return session;
    }

    private static Result channelExec(Session session, String command, Integer timeout,
                                      com.cowave.script.BaseScriptOutputStream outputStream, com.cowave.script.BaseScriptOutputStream extOutputStream) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setOutputStream(outputStream);
            channel.setExtOutputStream(extOutputStream);
            channel.connect(Optional.ofNullable(timeout).orElse(0));
            String output = IOUtils.toString(outputStream.getInputStream(), StandardCharsets.UTF_8);
            String error = IOUtils.toString(extOutputStream.getInputStream(), StandardCharsets.UTF_8);
            int exitCode = channel.getExitStatus();
            return new Result(exitCode == 0, exitCode, output, error);
        } catch (Exception e) {
            log.error("", e);
            return Result.applyError(-99, null, e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private static void channelSftp(Session session, InputStream inputStream, String remotePathFile) throws SftpException, JSchException {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            String path = remotePathFile.substring(0, remotePathFile.lastIndexOf("/") + 1);
            createDir(sftp, path);
            sftp.setInputStream(inputStream);
            sftp.put(inputStream, remotePathFile);
        } finally {
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
        }
    }


    /**
     * 创建一个文件目录
     */
    private static void createDir(ChannelSftp sftp, String createPath) {
        try {
            if (isDirExist(sftp, createPath)) {
                return;
            }
            String pwd = sftp.lpwd();
            StringBuilder filePath = new StringBuilder("/");
            for (String path : createPath.split("/")) {
                if ("".equals(path)) {
                    continue;
                }
                filePath.append(path).append("/");
                if (isDirExist(sftp, filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    sftp.mkdir(filePath.toString());
                    sftp.cd(filePath.toString());
                }
            }
            sftp.cd(pwd);
        } catch (SftpException e) {
            throw new UnsupportedOperationException("创建路径错误：" + createPath, e);
        }
    }

    /**
     * 判断目录是否存在
     */
    private static boolean isDirExist(ChannelSftp sftp, String directory) throws SftpException {
        try {
            SftpATTRS sftpAttrs = sftp.lstat(directory);
            return sftpAttrs.isDir();
        } catch (SftpException e) {
            if ("no such file".equalsIgnoreCase(e.getMessage())) {
                return false;
            }
            throw e;
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        boolean success;
        private int exitCode;
        private String output;
        private String error;

        public static Result applySuccess(String output) {
            return new Result(true, 0, output, null);
        }

        public static Result applyError(int exitCode, String output, String error) {
            return new Result(false, exitCode, output, error);
        }
    }

}