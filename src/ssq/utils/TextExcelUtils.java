package ssq.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TextExcelUtils {
  public static final String KEEP = "`keep`";
  public static final String OFFICE_EXCEL_2003_SUFFIX = "xls";
  public static final String OFFICE_EXCEL_2010_SUFFIX = "xlsx";

  /**
   * read the Excel file
   *
   * @param path
   *          the path of the Excel file
   * @return
   * @throws IOException
   */
  public static List<List<String>> readExcel(String path) throws IOException {
    if (!StringUtils.noContent(path)) {
      String suffix = FileUtils.getExt(path).trim();
      if (!StringUtils.noContent(suffix)) {
        if (OFFICE_EXCEL_2003_SUFFIX.equals(suffix)) {
          return readXls(path);
        } else if (OFFICE_EXCEL_2010_SUFFIX.equals(suffix)) { return readXlsx(path); }
      }
    }
    return null;
  }

  /**
   * Read the Excel 2010
   *
   * @param path
   *          the path of the excel file
   * @return
   * @throws IOException
   */
  private static List<List<String>> readXlsx(String path) throws IOException {
    InputStream is = new FileInputStream(path);
    XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);
    List<List<String>> list = new ArrayList<>();
    try {
      // Read the Sheet
      for (int numSheet = 0; numSheet < xssfWorkbook.getNumberOfSheets(); numSheet++) {
        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(numSheet);
        if (xssfSheet == null) {
          continue;
        }
        // Read the Row
        for (int rowNum = 0; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
          XSSFRow xssfRow = xssfSheet.getRow(rowNum);
          if (xssfRow != null) {
            List<String> rowList = new LinkedList<>();
            for (Cell cell : xssfRow) {
              rowList.add(getValue((XSSFCell) cell));
            }
            list.add(rowList);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      xssfWorkbook.close();
    }
    return list;
  }

  /**
   * Read the Excel 2003-2007
   *
   * @param path
   *          the path of the Excel
   * @return
   * @throws IOException
   */
  private static List<List<String>> readXls(String path) throws IOException {
    InputStream is = new FileInputStream(path);
    HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
    List<List<String>> list = new ArrayList<>();
    try {
      // Read the Sheet
      for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
        if (hssfSheet == null) {
          continue;
        }
        // Read the Row
        for (int rowNum = 0; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
          HSSFRow hssfRow = hssfSheet.getRow(rowNum);
          if (hssfRow != null) {
            List<String> rowList = new LinkedList<>();
            for (Cell cell : hssfRow) {
              rowList.add(getValue((HSSFCell) cell));
            }
            list.add(rowList);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      hssfWorkbook.close();
    }
    return list;
  }

  private static String getValue(XSSFCell xssfCell) {
    if (xssfCell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
      return String.valueOf(xssfCell.getBooleanCellValue());
    } else if (xssfCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
      return String.valueOf(xssfCell.getNumericCellValue());
    } else {
      return String.valueOf(xssfCell.getStringCellValue());
    }
  }

  private static String getValue(HSSFCell hssfCell) {
    if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
      return String.valueOf(hssfCell.getBooleanCellValue());
    } else if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
      return String.valueOf(hssfCell.getNumericCellValue());
    } else {
      return String.valueOf(hssfCell.getStringCellValue());
    }
  }

  public static HSSFWorkbook toExcel(List<String> rows, String delimiter) { // 创建excel文件对象
    if (KEEP.contains(delimiter)) {
      return null;
    }
    
    HSSFWorkbook wb = new HSSFWorkbook();
    Font titleFont = createFonts(wb, Font.BOLDWEIGHT_BOLD, "宋体", false, (short) 200);
    Font contentFont = createFonts(wb, Font.BOLDWEIGHT_NORMAL, "宋体", false, (short) 200);

    CellStyle titleStyle = wb.createCellStyle();
    titleStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
    titleStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_BOTTOM);
    titleStyle.setFont(titleFont);

    CellStyle contentStyle = wb.createCellStyle();
    contentStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
    contentStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_BOTTOM);
    contentStyle.setFont(contentFont);

    HSSFSheet sheet = wb.createSheet();

    for (int i = 0; i < rows.size() && i < 65536; i++) {
      Row rowData = sheet.createRow(i);
      String[] vals = rows.get(i).split(delimiter);

      if (i == 0) {
        vals = new String[] { rows.get(0) };
      }

      for (int j = 0; j < vals.length; j++) {
        createCell(wb, rowData, j, vals[j], i < 2 ? titleStyle : contentStyle);
      }
    }

    if (rows.size() > 1) {
      int len = (short) (rows.get(1).split(delimiter).length - 1);
      if (len > 0) {
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, len);
        sheet.addMergedRegion(region);
      }
    }

    return wb;
  }

  /**
   * 创建单元格并设置样式,值
   *
   * @param wb
   * @param row
   * @param column
   * @param
   * @param
   * @param value
   */
  private static void createCell(Workbook wb, Row row, int column, String value, CellStyle cellStyle) {
    Cell cell = row.createCell(column);

    Double tmp = null;
    
    if (value.startsWith(KEEP)) {
      value = value.substring(KEEP.length());
    }
    else{
      try {
        tmp = Double.valueOf(value);
      } catch (Exception e) {
      }
    }

    if (tmp != null) {
      cell.setCellValue(tmp);
    } else {
      cell.setCellValue(value);
    }
    cell.setCellStyle(cellStyle);
  }

  /**
   * 设置字体
   *
   * @param wb
   * @return
   */
  private static Font createFonts(Workbook wb, short bold, String fontName, boolean isItalic, short hight) {
    Font font = wb.createFont();
    font.setFontName(fontName);
    font.setBoldweight(bold);
    font.setItalic(isItalic);
    font.setFontHeight(hight);
    return font;
  }
}
