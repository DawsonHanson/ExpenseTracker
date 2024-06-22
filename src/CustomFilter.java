import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

class CustomFilter extends DocumentFilter {
  private String filterType;

  CustomFilter(String filterType) {
    this.filterType = filterType;
  }

  @Override
  public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {

    Document doc = fb.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.insert(offset, string);

    if (test(sb.toString())) {
      super.insertString(fb, offset, string, attr);
    }
  }

  private boolean test(String text) {
    boolean result = false;
    switch (filterType) {
      case "double":
        try {
          Double.parseDouble(text);
          result = true;
        } catch (NumberFormatException e) {
          result = false;
        }
      break;
      case "string":
        if(text.matches("[a-zA-Z0-9- ]*")) {
          result = true;
        } else {
          result = false;
        }
    }
    return result;
  }

  @Override
  public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

    Document doc = fb.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.replace(offset, offset + length, text);

    if (test(sb.toString())) {
      super.replace(fb, offset, length, text, attrs);
    }
  }

  @Override
  public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    Document doc = fb.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.delete(offset, offset + length);

    if (sb.toString().length() == 0) {
      super.replace(fb, offset, length, "", null);
    } else {
      if (test(sb.toString())) {
        super.remove(fb, offset, length);
      }
    }
  }
}