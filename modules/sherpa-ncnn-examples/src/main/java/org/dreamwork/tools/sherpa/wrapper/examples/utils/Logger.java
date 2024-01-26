package org.dreamwork.tools.sherpa.wrapper.examples.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger implements Runnable {
    final BlockingQueue<MessageWrapper> queue = new LinkedBlockingQueue<> ();
    final Thread thread;

    public Logger () {
        thread = new Thread (this);
        thread.setDaemon (true);
        thread.start ();
    }

    public void info (String pattern, Object... args) {
        try {
            queue.put (new MessageWrapper (pattern, args));
        } catch (InterruptedException e) {
            throw new RuntimeException (e);
        }
    }

    public void warn (String message, Throwable ex) {
        StringWriter writer = new StringWriter (1024);
        PrintWriter pw = new PrintWriter (writer);
        ex.printStackTrace (pw);
        try {
            MessageWrapper w = new MessageWrapper (message);
            w.type = "error";
            queue.put (w);

            w = new MessageWrapper (writer.toString ());
            w.type = "error";
            queue.put (w);
        } catch (InterruptedException ie) {
            throw new RuntimeException (ie);
        }
    }

    @Override
    public void run () {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
        while (!thread.isInterrupted ()) {
            MessageWrapper w;
            try {
                w = queue.take ();
            } catch (InterruptedException e) {
                throw new RuntimeException (e);
            }
            String pattern = "[%s][%s] - " + w.pattern + "%n";
            Object[] args = new Object[2 + w.args.length];
            args[0] = sdf.format (System.currentTimeMillis ());
            args[1] = w.name;
            if (w.args.length > 0) {
                System.arraycopy (w.args, 0, args, 2, w.args.length);
            }

            if ("error".equals (w.type)) {
                System.err.printf (pattern, args);
            } else {
                System.out.printf (pattern, args);
            }
        }
    }
}
