package vgtimer;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config
{
    private Properties properties;

    public Config(String filename)
    {
        properties = new Properties();

        try
        {
            InputStream stream = new FileInputStream(filename);
            properties.load(stream);
        }
        catch (IOException e)
        {
            System.err.println("Unable to read configuration from " + filename + ":");
            e.printStackTrace();
        }
    }

    protected KeyStroke parseKeys(String keys, String defaultKey)
    {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keys);

        if (keyStroke != null)
            return keyStroke;

        System.err.println("Unable to parse '" + keys + "', using default (" + defaultKey + ")");
        return KeyStroke.getKeyStroke(defaultKey);
    }

    public KeyStroke getResetKeys()
    {
        return parseKeys(properties.getProperty("reset"), "control F9");
    }

    public KeyStroke getStopKeys()
    {
        return parseKeys(properties.getProperty("stop"), "control F10");
    }

    public KeyStroke getRestartGreenKeys()
    {
        return parseKeys(properties.getProperty("green"), "8");
    }

    public KeyStroke getRestartBreakKeys()
    {
        return parseKeys(properties.getProperty("break"), "9");
    }

    public KeyStroke getRestartSegmentKeys()
    {
        return parseKeys(properties.getProperty("segment"), "0");
    }

    public KeyStroke getRestartBlueKeys()
    {
        return parseKeys(properties.getProperty("blue"), "MINUS");
    }
}
