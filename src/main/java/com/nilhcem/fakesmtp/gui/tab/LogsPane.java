package com.nilhcem.fakesmtp.gui.tab;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.gui.info.ClearAllButton;
import com.nilhcem.fakesmtp.log.SMTPLogsAppender;
import com.nilhcem.fakesmtp.log.SMTPLogsObservable;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

/**
 * Scrolled text area where will be displayed the SMTP logs.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class LogsPane implements Observer {

	private final JScrollPane logsPane = new JScrollPane();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
	private final RSyntaxTextArea logsArea = new RSyntaxTextArea();
	// private final JTextArea logsArea = new JTextArea();

	/**
	 * Gets prefered font if found
	 */
	public String getDefaultFontName() {
		final String defaultFont = "PragmataPro";
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		if (Arrays.asList(fonts).contains(defaultFont)) {
			return defaultFont;
		}

		return "Monospace";
	}

	/**
	 * Creates the text area, sets it as non-editable and sets an observer to
	 * intercept logs.
	 */
	public LogsPane() {
		final String defaultFont = getDefaultFontName();
		final Font plainFont = new Font(defaultFont, Font.PLAIN, 14);
		logsArea.setFont(plainFont);
		logsArea.setEditable(false);

		if (logsArea instanceof RSyntaxTextArea) {
			logsArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
			logsArea.setCodeFoldingEnabled(true);
			SyntaxScheme scheme = logsArea.getSyntaxScheme();
			final Font boldFont = new Font(defaultFont, Font.BOLD, 14);
			scheme.getStyle(Token.RESERVED_WORD).font = boldFont;
			logsArea.revalidate();
		}

		logsPane.getViewport().add(logsArea, null);
		addObserverToSmtpLogAppender();
	}

	/**
	 * Returns the JScrollPane object.
	 *
	 * @return the JScrollPane object.
	 */
	public JScrollPane get() {
		return logsPane;
	}

	/**
	 * Adds this object to the SMTP logs appender observable, to intercept logs.
	 * <p>
	 * The goal is to be informed when the log appender will received some debug
	 * SMTP logs.<br>
	 * When a log is written, the appender will notify this class which will display
	 * it in the text area.
	 * </p>
	 */
	private void addObserverToSmtpLogAppender() {
		Logger smtpLogger = LoggerFactory.getLogger(org.subethamail.smtp.server.Session.class);
		String appenderName = Configuration.INSTANCE.get("logback.appender.name");

		@SuppressWarnings("unchecked")
		SMTPLogsAppender<ILoggingEvent> appender = (SMTPLogsAppender<ILoggingEvent>) ((AppenderAttachable<ILoggingEvent>) smtpLogger)
				.getAppender(appenderName);
		if (appender == null) {
			LoggerFactory.getLogger(LogsPane.class).error("Can't find logger: {}", appenderName);
		} else {
			appender.getObservable().addObserver(this);
		}
	}

	/**
	 * Updates the content of the text area.
	 * <p>
	 * This method will be called by an observable element.
	 * </p>
	 * <ul>
	 * <li>If the observable is a {@link SMTPLogsObservable} object, the text area
	 * will display the received log.</li>
	 * <li>If the observable is a {@link ClearAllButton} object, the text area will
	 * be cleared.</li>
	 * </ul>
	 *
	 * @param o
	 *            the observable element which will notify this class.
	 * @param log
	 *            optional parameter (a {@code String} object, when the observable
	 *            is a {@code SMTPLogsObservable} object, which will contain the
	 *            log).
	 */
	@Override
	public void update(Observable o, Object log) {
		if (o instanceof SMTPLogsObservable) {
			// Update and scroll pane to the bottom
			logsArea.append(String.format("%s - %s%n", dateFormat.format(new Date()), log));
			logsArea.setCaretPosition(logsArea.getText().length());
		} else if (o instanceof ClearAllButton) {
			// Remove text
			logsArea.setText("");
		}
	}
}
