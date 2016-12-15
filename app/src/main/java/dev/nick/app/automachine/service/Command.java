package dev.nick.app.automachine.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.WorkerThread;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import dev.nick.app.automachine.IInputInjector;
import dev.nick.app.automachine.R;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public abstract class Command {

    public abstract Element onCreateXmlElement();

    public String getName() {
        return getClass().getSimpleName();
    }

    public String getDescription() {
        return null;
    }

    public int getIconRes() {
        return R.mipmap.ic_launcher;
    }

    public static class Commander implements Runnable {

        private final Queue<Command> mCommands;
        private Context mContext;
        private Logger mLogger;

        private int mCount;
        private String mName;

        public Commander(Context context) {
            mCommands = new LinkedList<>();
            mContext = context;
            mLogger = LoggerManager.getLogger(getClass());
        }

        public static Commander fromXml(Context context, String xmlPath) {
            try {
                return read(context, new FileInputStream(xmlPath));
            } catch (Exception e) {

            }
            return null;
        }

        private static Commander read(Context context, InputStream stream) throws Exception {

            Commander commander = new Commander(context);

            SAXBuilder builder = new SAXBuilder();
            InputSource source = new InputSource(stream);
            Document document = builder.build(source);
            Element root = document.getRootElement();

            Attribute countAttr = root.getAttribute("count");
            commander.count(countAttr.getIntValue());

            List<Element> childrenList = root.getChildren();
            for (Element e : childrenList) {
                String name = e.getQualifiedName();
                switch (name) {
                    case "home":
                        commander.home();
                        break;
                    case "back":
                        commander.back();
                        break;
                    case "sleep":
                        Element tE = e.getChild("time");
                        commander.sleep(Long.parseLong(tE.getValue()));
                        break;
                    case "tap":
                        Element xE = e.getChild("x");
                        String x = xE.getValue();
                        Element yE = e.getChild("y");
                        String y = yE.getValue();
                        commander.tap(Integer.parseInt(x), Integer.parseInt(y));
                        break;
                    default:
                        throw new IllegalArgumentException("Bad tag");
                }
            }
            return commander;
        }

        public String getName() {
            return mName;
        }

        public Queue<Command> getCommands() {
            return mCommands;
        }

        public int getCount() {
            return mCount;
        }

        @Override
        public String toString() {
            return "Commander{" +
                    "mCommands=" + mCommands +
                    '}';
        }

        public Commander count(int c) {
            mCount = c;
            mLogger.debug("-count -" + c);
            return this;
        }

        public Commander name(String n) {
            mName = n;
            mLogger.debug("-name -" + n);
            return this;
        }

        public Commander home() {
            mCommands.add(new Home());
            mLogger.debug("-home");
            return this;
        }

        public Commander command(Command c) {
            mCommands.add(c);
            return this;
        }

        public Commander sleep(long time) {
            mCommands.add(new Sleep(time));
            mLogger.debug("-sleep -" + time);
            return this;
        }

        public Commander back() {
            mCommands.add(new Back());
            mLogger.debug("-back");
            return this;
        }

        public Commander tap(int x, int y) {
            mCommands.add(new Tap(x, y));
            mLogger.debug("-tap -x" + x + " -y" + y);
            return this;
        }

        @WorkerThread
        public void execute() {

            if (mCount < 0) {
                throw new IllegalArgumentException("Set count first");
            }

            mContext.bindService(new Intent(mContext, InputInjectionService.class), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    IInputInjector service = IInputInjector.Stub.asInterface(iBinder);
                    for (int i = 0; i < mCount; i++) {
                        scheduleCommands(service);
                    }
                    mContext.unbindService(this);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            }, Context.BIND_AUTO_CREATE);
        }

        private void scheduleCommands(IInputInjector service) {
            for (Command command : mCommands) {

                mLogger.debug("CMD:" + command.getName());

                if (command instanceof Home) {
                    try {
                        service.home();
                    } catch (RemoteException e) {

                    }
                } else if (command instanceof Sleep) {
                    Sleep sleep = (Sleep) command;
                    try {
                        Thread.sleep(sleep.time);
                    } catch (InterruptedException e) {

                    }
                } else if (command instanceof Back) {
                    try {
                        service.back();
                    } catch (RemoteException e) {

                    }
                } else if (command instanceof Tap) {
                    Tap tap = (Tap) command;
                    try {
                        service.tap(tap.x, tap.y);
                    } catch (RemoteException e) {

                    }
                }
            }
        }

        @Override
        public void run() {
            execute();
        }

        public void buildXMLDoc(String destDir) throws IOException, JDOMException {
            if (mCount < 0) {
                throw new IllegalArgumentException("Set count first");
            }
            if (mName == null) {
                throw new IllegalArgumentException("Set name first");
            }
            Element root = new Element("commands").setAttribute("count", String.valueOf(mCount));
            Document doc = new Document(root);
            for (Command c : mCommands) {
                Element elements = c.onCreateXmlElement();
                root.addContent(elements);
            }
            Format format = Format.getPrettyFormat();
            XMLOutputter XMLOut = new XMLOutputter(format);
            String path = destDir + "/" + getName() + ".xml";
            File file = new File(path);
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IllegalStateException("Fail to mkdirs.");
            }
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            XMLOut.output(doc, new FileOutputStream(path));
        }
    }

    public static class Home extends Command {
        @Override
        public Element onCreateXmlElement() {
            return new Element("home");
        }

        @Override
        public int getIconRes() {
            return R.drawable.ic_panorama_wide_angle_black_24dp;
        }
    }

    public static class Back extends Command {

        @Override
        public int getIconRes() {
            return R.drawable.ic_group_work_black_24dp;
        }

        @Override
        public Element onCreateXmlElement() {
            return new Element("back");
        }
    }

    public static class Sleep extends Command {

        long time;

        public Sleep(long time) {
            this.time = time;
        }

        @Override
        public Element onCreateXmlElement() {
            Element element = new Element("sleep");
            element.addContent(new Element("time").setText(String.valueOf(time)));
            return element;
        }

        @Override
        public String getDescription() {
            return String.valueOf(time) + "ms";
        }

        @Override
        public int getIconRes() {
            return R.drawable.ic_hotel_black_24dp;
        }
    }

    public static class Tap extends Command {

        int x;
        int y;

        public Tap(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String getDescription() {
            return String.valueOf(x) + "-" + String.valueOf(y);
        }

        @Override
        public int getIconRes() {
            return R.drawable.ic_touch_app_black_24dp;
        }

        @Override
        public Element onCreateXmlElement() {
            Element element = new Element("tap");
            element.addContent(new Element("x").setText(String.valueOf(x)));
            element.addContent(new Element("y").setText(String.valueOf(y)));
            return element;
        }
    }
}
