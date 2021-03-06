package osmedile.intellij.stringmanip.sort;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import osmedile.intellij.stringmanip.MyEditorWriteActionHandler;
import osmedile.intellij.stringmanip.sort.support.SortLines;
import osmedile.intellij.stringmanip.sort.support.SortSettings;
import osmedile.intellij.stringmanip.sort.support.SortTypeDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SortAction extends EditorAction {
	public static final String STORE_KEY = "StringManipulation.SortAction.SortSettings";
	private String storeKey = STORE_KEY;

	public SortAction(String storeKey) {
		this();
		this.storeKey = storeKey;
	}

	protected SortAction() {
		this(true);
	}

	protected SortAction(boolean setupHandler) {
		super(null);
		this.setupHandler(new MyEditorWriteActionHandler<SortSettings>() {
			@NotNull
			@Override
			protected Pair beforeWriteAction(Editor editor, DataContext dataContext) {
				SortSettings settings = getSortSettings(editor);
				if (settings == null) return stopExecution();

				return continueExecution(settings);
			}

			@Override
			protected void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext, SortSettings settings) {
				List<CaretState> caretsAndSelections = editor.getCaretModel().getCaretsAndSelections();

				if (caretsAndSelections.size() > 1) {
					processMultiCaret(editor, caretsAndSelections, settings);
				} else if (caretsAndSelections.size() == 1) {
					processSingleSelection(editor, caretsAndSelections, settings);
				}
			}

		});
	}

	@SuppressWarnings("Duplicates")
	@Nullable
	protected SortSettings getSortSettings(final Editor editor) {
		final SortTypeDialog dialog = new SortTypeDialog(getSortSettings(storeKey), true);
		DialogWrapper dialogWrapper = new DialogWrapper(editor.getProject()) {
			{
				init();
				setTitle("Sort Settings");
			}

			@Nullable
			@Override
			public JComponent getPreferredFocusedComponent() {
				return dialog.insensitive;
			}

			@Nullable
			@Override
			protected String getDimensionServiceKey() {
				return "StringManipulation.SortTypeDialog";
			}

			@Nullable
			@Override
			protected JComponent createCenterPanel() {
				return dialog.contentPane;
			}


			@Override
			protected void doOKAction() {
				super.doOKAction();
			}
		};

		boolean b = dialogWrapper.showAndGet();
		if (!b) {
			return null;
		}
		SortSettings settings = dialog.getSettings();
		settings.store(storeKey);
		return settings;
	}

	protected SortSettings getSortSettings(String storeKey) {
		return SortSettings.readFromStore(storeKey);
	}

	private void processSingleSelection(Editor editor, List<CaretState> caretsAndSelections, SortSettings settings) {
		CaretState caretsAndSelection = caretsAndSelections.get(0);
		LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
		LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();
		String text = editor.getDocument().getText(
				new TextRange(editor.logicalPositionToOffset(selectionStart),
						editor.logicalPositionToOffset(selectionEnd)));

		String charSequence = new SortLines(text, settings).sort();

		editor.getDocument().replaceString(editor.logicalPositionToOffset(selectionStart),
				editor.logicalPositionToOffset(selectionEnd), charSequence);
	}

	private void processMultiCaret(Editor editor, List<CaretState> caretsAndSelections, SortSettings settings) {
		List<String> lines = new ArrayList<String>();
		for (CaretState caretsAndSelection : caretsAndSelections) {
			LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
			LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();
			String text = editor.getDocument().getText(
					new TextRange(editor.logicalPositionToOffset(selectionStart),
							editor.logicalPositionToOffset(selectionEnd)));
			lines.add(text);
		}

		lines = new SortLines(lines, settings).sortLines();

		for (int i = caretsAndSelections.size() - 1; i >= 0; i--) {
			CaretState caretsAndSelection = caretsAndSelections.get(i);
			LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
			LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();


			if (lines.size() > i) {
				String line = lines.get(i);
				editor.getDocument().replaceString(editor.logicalPositionToOffset(selectionStart),
						editor.logicalPositionToOffset(selectionEnd), line);
			} else {
				editor.getDocument().deleteString(editor.logicalPositionToOffset(selectionStart), editor.logicalPositionToOffset(selectionEnd));
				Caret caretAt = editor.getCaretModel().getCaretAt(editor.logicalToVisualPosition(selectionStart));
				editor.getCaretModel().removeCaret(caretAt);
			} 
		}

	}
}
