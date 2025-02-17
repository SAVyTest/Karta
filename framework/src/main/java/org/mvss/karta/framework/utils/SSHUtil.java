package org.mvss.karta.framework.utils;

import com.jcraft.jsch.*;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.configuration.ProxyOptions;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
@Getter
@SuppressWarnings("unused")
public class SSHUtil implements AutoCloseable {
    public static final String EXEC = "exec";
    public static final String SUDO_S_P = "sudo -S -p '' ";
    public static final String MKTEMP = "mktemp";
    public static final String CP_NO_PRESERVE_MODE_OWNERSHIP = "cp --no-preserve=mode,ownership ";
    public static final String EXCEPTION_OCCURRED = "Exception occurred";
    public static final String SCP_F = "scp -f ";
    public static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    public static final String NO = "no";
    public static final String LOCALHOST = "127.0.0.1";


    protected transient JSch jsch = new JSch();
    protected transient Session session = null;

    protected SSHUtil jumpSSHUtil;
    protected int currentLocalJumpSSHPort;

    protected String user;
    protected String pass;
    protected String host = LOCALHOST;
    protected int port = 22;

    protected ProxyOptions proxyOptions;

    public SSHUtil() {

    }

    public SSHUtil(String user, String pass, String host) throws JSchException {
        this(user, pass, host, 22);
    }

    public SSHUtil(String user, String pass, String host, int port) throws JSchException {
        init(user, pass, host, port);
    }

    public SSHUtil(String user, String pass, String host, int port, SSHUtil jumpSSHUtil) throws JSchException {
        init(user, pass, host, port, jumpSSHUtil);
    }

    @Builder
    public SSHUtil(String user, String pass, String host, int port, SSHUtil jumpSSHUtil, ProxyOptions proxyOptions) throws JSchException {
        init(user, pass, host, port, jumpSSHUtil, proxyOptions);
    }

    public void connect() throws JSchException {

        if (session == null) {
            if (jumpSSHUtil != null) {
                jumpSSHUtil.connect();
                currentLocalJumpSSHPort = jumpSSHUtil.session.setPortForwardingL(0, this.host, this.port);
                session = jsch.getSession(user, LOCALHOST, currentLocalJumpSSHPort);
            } else {
                session = jsch.getSession(user, host, port);
            }
            if ((session == null) || (!session.isConnected())) {
                throw new JSchException("Session is null or not connected even after initialization");
            }
        }

        if (!session.isConnected()) {
            UserInfo ui = new ByPassUserInfo(pass);
            session.setUserInfo(ui);

            Properties config = new Properties();
            config.setProperty(STRICT_HOST_KEY_CHECKING, NO);
            session.setConfig(config);

            if (proxyOptions != null) {
                ProxyHTTP proxyHTTP = new ProxyHTTP(proxyOptions.getHost(), proxyOptions.getPort());
                if (proxyOptions.isProxyAuthentication()) {
                    proxyHTTP.setUserPasswd(proxyOptions.getUsername(), proxyOptions.getPassword());
                }
                session.setProxy(proxyHTTP);
            }

            session.connect();
        }
    }

    public synchronized void reconnect() throws JSchException {
        jumpSSHUtil.reconnect();
        session = null;
        connect();
    }

    protected void init(String user, String pass, String host, int port, SSHUtil jumpSSHUtil, ProxyOptions proxyOptions) throws JSchException {
        this.user = user;
        this.pass = pass;
        this.host = host;
        this.port = port;
        this.jumpSSHUtil = jumpSSHUtil;
        this.proxyOptions = proxyOptions;
        // Lazy connect: Need not connect at init connect();
    }

    protected void init(String user, String pass, String host, int port, SSHUtil jumpSSHUtil) throws JSchException {
        init(user, pass, host, port, jumpSSHUtil, null);
    }

    protected void init(String user, String pass, String host, int port) throws JSchException {
        init(user, pass, host, port, null, null);
    }

    @Override
    public void close() {
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
            session = null;
        }
    }

    /**
     * Internal method for checking acknowledgement
     */
    private static int checkAck(InputStream in) throws Exception {
        int b = in.read();

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');

            throw new Exception("Scp error: " + sb);
        }

        return b;
    }

    public int executeCommand(String command) throws Exception {
        return executeCommand(command, System.out);
    }

    public int executeCommand(String command, boolean sudo) throws Exception {
        connect();
        return sudo ? executeSudoCommand(command, System.out) : executeCommand(command, System.out);
    }

    public int executeCommand(String command, PrintStream outputStream) throws Exception {
        connect();
        int exitCode;
        Channel channel = session.openChannel(EXEC);
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        InputStream in = channel.getInputStream();
        ((ChannelExec) channel).setErrStream(outputStream != null ? outputStream : System.err, true);
        channel.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                if (outputStream != null) {
                    outputStream.print(new String(tmp, 0, i));
                }
            }
            if (channel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                exitCode = channel.getExitStatus();
                break;
            }
            //noinspection BusyWait
            Thread.sleep(1000);
        }
        channel.disconnect();
        return exitCode;
    }

    public int executeSudoCommand(String command) throws Exception {
        return executeSudoCommand(command, System.out);
    }

    public String executeCommandReturningOutput(String command, boolean useSudo) throws Exception {
        connect();
        String output;

        log.debug("Running command " + command + " sudo=" + useSudo);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            if (useSudo) {
                executeSudoCommand(command, new PrintStream(byteArrayOutputStream));
            } else {
                executeCommand(command, new PrintStream(byteArrayOutputStream));
            }
            output = byteArrayOutputStream.toString();
        }

        return output;
    }

    public Future<String> executeCommandReturningOutputFuture(String command, boolean useSudo, ExecutorService executor) {
        return executor.submit(() -> executeCommandReturningOutput(command, useSudo));
    }

    public String executeCommandWithTimeout(String command, boolean useSudo, long timeOut, long checkInterval) throws Throwable {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> futureOutput = executeCommandReturningOutputFuture(command, useSudo, executor);
        String output = null;

        log.info("Going to wait for the command to return output...");
        WaitResult waitResult = WaitUtil.waitUntil(futureOutput::isDone, timeOut, checkInterval, WaitUtil.defaultWaitIterationTask);
        executor.shutdown();

        if (waitResult.isSuccessful() && futureOutput.isDone()) {
            output = futureOutput.get();
        }

        return output;
    }

    public int executeSudoCommand(String command, PrintStream outputStream) throws Exception {
        connect();
        int exitCode;
        Channel channel = session.openChannel(EXEC);
        ((ChannelExec) channel).setCommand(SUDO_S_P + command);
        InputStream in = channel.getInputStream();
        OutputStream out = channel.getOutputStream();

        ((ChannelExec) channel).setErrStream(outputStream != null ? outputStream : System.err, true);

        channel.connect();

        out.write((pass + "\n").getBytes());
        out.flush();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                if (outputStream != null) {
                    outputStream.print(new String(tmp, 0, i));
                }
            }
            if (channel.isClosed()) {
                exitCode = channel.getExitStatus();
                break;
            }
            //noinspection BusyWait
            Thread.sleep(1000);
        }
        channel.disconnect();
        return exitCode;
    }

    public int executeSudoCommandWithoutOutput(String command) throws Exception {
        Channel channel = session.openChannel(EXEC);

        ((ChannelExec) channel).setCommand(SUDO_S_P + command);

        // InputStream in = channel.getInputStream();
        OutputStream out = channel.getOutputStream();
        ((ChannelExec) channel).setErrStream(System.err, true);

        channel.connect();

        out.write((pass + "\n").getBytes());
        out.flush();
        int exitCode = channel.getExitStatus();
        channel.disconnect();
        return exitCode;
    }

    public boolean getFile(String remoteFile, String localFile, boolean useSudo) {
        try {
            if (useSudo) {
                String tempFile = executeCommandReturningOutput(MKTEMP, false).trim();

                if (executeSudoCommand(CP_NO_PRESERVE_MODE_OWNERSHIP + remoteFile + " " + tempFile) != 0) {
                    return false;
                }

                remoteFile = tempFile;
            }
            getFile(remoteFile, localFile);
            return true;
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURRED, e);
            return false;
        }
    }

    public void getFile(String remoteFile, String localFile) throws Exception {
        FileOutputStream fos = null;
        try {
            String prefix = null;
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator;
            }

            // exec 'scp -f rightFile' remotely
            remoteFile = remoteFile.replace("'", "'\"'\"'");
            remoteFile = "'" + remoteFile + "'";
            String command = SCP_F + remoteFile;
            Channel channel = session.openChannel(EXEC);
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                int read = in.read(buf, 0, 5);

                long fileSize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    fileSize = fileSize * 10L + buf[0] - '0';
                }

                String file;
                for (int i = 0; ; i++) {
                    int read1 = in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of leftFile
                fos = new FileOutputStream(prefix == null ? localFile : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < fileSize) foo = buf.length;
                    else foo = (int) fileSize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    fileSize -= foo;
                    if (fileSize == 0L) break;
                }
                fos.close();
                fos = null;

                if (checkAck(in) != 0) {
                    throw new Exception("Acknowledgement check failed");
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }

            channel.disconnect();

        } catch (Exception e) {
            log.info(e);
            if (fos != null) {
                fos.close();
            }

            throw e;
        }
    }

    public void uploadFolder(String localFolder, String remoteFolder) throws Exception {
        if (executeCommand("mkdir -p " + remoteFolder) != 0) {
            return;
        }

        File folder = new File(localFolder);
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;

        for (File listOfFile : listOfFiles) {
            String baseName = listOfFile.getName();
            String absolutePath = listOfFile.getAbsolutePath();

            if (listOfFile.isFile()) {
                uploadFile(absolutePath, remoteFolder + "/" + baseName);
            } else if (listOfFile.isDirectory()) {
                uploadFolder(absolutePath, remoteFolder + "/" + baseName);
            }
        }
    }

    public int runFile(String localFileName, String remoteFileName, String args, boolean sudo) throws Exception {
        connect();
        uploadFile(localFileName, remoteFileName);
        String command = "bash " + remoteFileName + " " + args;
        return executeCommand(command, sudo);
    }

    /**
     * Method to upload a local file to SSH server
     */
    public void uploadFile(String localFile, String remoteFile) throws Exception {
        connect();
        FileInputStream fis;

        String remoteBaseFileName = remoteFile;

        if (remoteFile.lastIndexOf('/') > 0) {
            remoteBaseFileName = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);
        }

        // exec 'scp -t rightFile' remotely
        remoteFile = remoteFile.replace("'", "'\"'\"'");
        remoteFile = "'" + remoteFile + "'";
        String command = "scp  -t " + remoteFile;
        Channel channel = session.openChannel(EXEC);
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            throw new Exception("Acknowledgement check failed");
        }

        File _leftFile = new File(localFile);

        // send "C0644 fileSize filename", where filename should not include '/'
        long fileSize = _leftFile.length();
        command = "C0644 " + fileSize + " ";
        command += remoteBaseFileName;
        command += "\n";
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) {
            throw new Exception("Acknowledgement check failed");
        }

        // send a content of leftFile
        fis = new FileInputStream(localFile);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0) break;
            out.write(buf, 0, len); // out.flush();
        }
        fis.close();
        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
        if (checkAck(in) != 0) {
            throw new Exception("Acknowledgement check failed");
        }
        out.close();
        channel.disconnect();
    }

    public boolean uploadFile(String localFileName, String remoteFileName, boolean useSudo) throws Exception {
        if (useSudo) {
            String tempFile = executeCommandReturningOutput(MKTEMP, false).trim();
            uploadFile(localFileName, tempFile);
            executeCommand("sudo mv " + tempFile + " " + remoteFileName, true);
        } else {
            uploadFile(localFileName, remoteFileName);
        }

        return true;
    }

    public void writeFile(byte[] content, String remoteFile) {

        InputStream fis = null;
        try {
            String remoteBaseFileName = remoteFile;

            if (remoteFile.lastIndexOf('/') > 0) {
                remoteBaseFileName = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);
            }

            // exec 'scp -t rightFile' remotely
            remoteFile = remoteFile.replace("'", "'\"'\"'");
            remoteFile = "'" + remoteFile + "'";
            String command = "scp  -t " + remoteFile;
            Channel channel = session.openChannel(EXEC);
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                throw new Exception("Acknowledgement check failed");
            }

            long modifiedTime = System.currentTimeMillis();

            // send "C0644 fileSize filename", where filename should not include '/'
            long fileSize = content.length;
            command = "C0644 " + fileSize + " ";
            command += remoteBaseFileName;
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new Exception("Acknowledgement check failed");
            }

            // send a content of leftFile
            fis = new ByteArrayInputStream(content);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); // out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                throw new Exception("Acknowledgement check failed");
            }
            out.close();
            channel.disconnect();
        } catch (Exception e) {
            log.info(e);
            try {
                if (fis != null) fis.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Method to upload a local file to SSH server
     */
    public void uploadStreamAsFile(InputStream is, long fileSize, String remoteFile) {
        try {
            String remoteBaseFileName = remoteFile;

            if (remoteFile.lastIndexOf('/') > 0) {
                remoteBaseFileName = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);
            }

            // exec 'scp -t rightFile' remotely
            remoteFile = remoteFile.replace("'", "'\"'\"'");
            remoteFile = "'" + remoteFile + "'";
            String command = "scp  -t " + remoteFile;
            Channel channel = session.openChannel(EXEC);
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                throw new Exception("Acknowledgement check failed");
            }

            // send "C0644 fileSize filename", where filename should not include '/'
            command = "C0644 " + fileSize + " " + remoteBaseFileName;

            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new Exception("Acknowledgement check failed");
            }

            byte[] buf = new byte[1024];
            while (true) {
                int len = is.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); // out.flush();
            }
            is.close();
            is = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                throw new Exception("Acknowledgement check failed");
            }
            out.close();
            channel.disconnect();
        } catch (Exception e) {
            log.info(e);
            try {
                if (is != null) is.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * This class is used internally to pass user password to JSch library
     */
    private static class ByPassUserInfo implements UserInfo {
        private final String password;

        public ByPassUserInfo(String password) {
            super();
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String message) {
            return true;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public boolean promptYesNo(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
        }
    }
}
