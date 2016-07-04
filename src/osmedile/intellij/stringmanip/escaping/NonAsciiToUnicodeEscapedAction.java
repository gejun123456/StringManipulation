package osmedile.intellij.stringmanip.escaping;

import osmedile.intellij.stringmanip.AbstractStringManipAction;
import osmedile.intellij.stringmanip.utils.StringUtil;

/**
 * @author Olivier Smedile
 * @version $Id: EscapeHtmlAction.java 16 2008-03-20 19:21:43Z osmedile $
 */
public class NonAsciiToUnicodeEscapedAction extends AbstractStringManipAction {

    public String transformByLine(String s) {
        return StringUtil.nonAsciiToUnicode(s);
    }
}