package com.nilhcem.fakesmtp.gui.tab;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import com.nilhcem.fakesmtp.gui.info.ClearAllButton;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.server.MailSaver;

/**
 * Scrolled text area where will be displayed the last received email.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class LastMailPane implements Observer {

	private final RTextScrollPane lastMailPane = new RTextScrollPane();
	private final RSyntaxTextArea lastMailArea = new RSyntaxTextArea();

	// private final JTextArea lastMailArea = new JTextArea();

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
	 * Creates the text area and disables the possibility to edit it.
	 */
	public LastMailPane() {
		final String defaultFont = getDefaultFontName();
		final Font plainFont = new Font(defaultFont, Font.PLAIN, 14);
		lastMailArea.setFont(plainFont);
		lastMailArea.setEditable(false);

		if (lastMailArea instanceof RSyntaxTextArea) {
			final Font boldFont = new Font(defaultFont, Font.BOLD, 14);
			lastMailArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
			lastMailArea.setCodeFoldingEnabled(true);
			lastMailArea.setMarkOccurrences(true);
			lastMailArea.setAntiAliasingEnabled(true);
			lastMailArea.setAutoscrolls(true);

			SyntaxScheme scheme = lastMailArea.getSyntaxScheme();
			scheme.getStyle(Token.RESERVED_WORD).font = boldFont;
			lastMailArea.revalidate();
		}

		lastMailPane.getViewport().add(lastMailArea, null);
	}

	/**
	 * Returns the JScrollPane object.
	 *
	 * @return the JScrollPane object.
	 */
	public JScrollPane get() {
		return lastMailPane;
	}

	/**
	 * Updates the content of the text area.
	 * <p>
	 * This method will be called by an observable element.
	 * </p>
	 * <ul>
	 * <li>If the observable is a {@link MailSaver} object, the text area will
	 * contain the content of the last received email;</li>
	 * <li>If the observable is a {@link ClearAllButton} object, the text area will
	 * be cleared.</li>
	 * </ul>
	 *
	 * @param o
	 *            the observable element which will notify this class.
	 * @param data
	 *            optional parameters (an {@code EmailModel} object, for the case of
	 *            a {@code MailSaver} observable).
	 */
	@Override
	public synchronized void update(Observable o, Object data) {
		if (o instanceof MailSaver) {
			EmailModel model = (EmailModel) data;
			lastMailArea.setText(model.getEmailStr());
		} else if (o instanceof ClearAllButton) {
			lastMailArea.setText("");
		}
	}

	/**
	 * performs text search
	 */
	public void searchForText(String text) {
		if (text != null && text.length() > 0) {
			SearchContext context = new SearchContext();
			context.setSearchFor(text);
			context.setSearchForward(true);
			context.setWholeWord(false);
			context.setMatchCase(false);
			context.setRegularExpression(false);

			SearchResult sr = SearchEngine.find(lastMailArea, context);
			if (!sr.wasFound()) {
				lastMailArea.setCaretPosition(0);
				SearchEngine.find(lastMailArea, context);
			}
		}
	}

	public void initTestPane() {
		lastMailArea.setText("hello world test this is a test for a message test");
	}
}
