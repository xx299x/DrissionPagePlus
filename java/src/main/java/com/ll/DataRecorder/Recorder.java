package com.ll.DataRecorder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Recorder extends BaseRecorder {

    private String delimiter = ",";
    private String quoteChar = "\"";
    private boolean followStyles = false;
    private Float colHeight = null;
    private String style = null;
    private boolean fitHead = false;

    // 其他属性和方法的声明
    public Recorder(String path) {
        this(path, null);
    }

    public Recorder(Path path) {
        this(path.toString(), null);
    }

    public Recorder(String path, Integer cacheSize) {
        super(path, cacheSize);
    }

    // 其他方法和属性的具体实现

    public RecorderSetter set() {
        if (setter == null) {
            setter = new RecorderSetter(this);
        }
        return (RecorderSetter) setter;
    }

    @Override
    public void addData(Object data) {
        this.addData(data, null);
    }

    /**
     * @return 返回csv文件分隔符
     */
    public String delimiter() {
        return delimiter;
    }

    /**
     * @return 返回csv文件引用符
     */
    public String quoteChar() {
        return quoteChar;
    }

    public void addData(Object data, String table) {
        while (this.pauseAdd) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!(data instanceof List) && !(data instanceof Map)) {
            data = Collections.singletonList(data);
        }

        if (data instanceof List<?> && ((List<?>) data).isEmpty()) {
            data = new ArrayList<>();
            this.dataCount++;
        }

        if (!"xlsx".equals(type)) {
            if (data instanceof List<?>) {
                this.data.addAll((List<?>) data);
            }
        } else {
            if (table == null) {
                table = this.table;
            } else if (table.equals("false")) {
                table = null;
            }

//            this.data.add(table, k -> new ArrayList<>()).addAll((List<?>) data);
        }

        if (0 < this.cache && cache <= dataCount) {
            record();
        }
    }

    protected void _record() {
//        if ("csv".equals(type)) {
//            toCsv();
//        } else if ("xlsx".equals(type)) {
//            toXlsx();
//        } else if ("json".equals(type)) {
//            toJson();
//        } else if ("txt".equals(type)) {
//            toTxt();
//        }
    }

//    protected void toXlsx() {
//        Path filePath = Paths.get(path);
//        boolean newFile = filePath.toFile().exists();
//        Workbook wb;
//        if (newFile) {
//            try {
//                wb = WorkbookFactory.create(filePath.toFile());
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//        } else {
//            wb = new XSSFWorkbook();
//        }
//
//        List<String> tables = new ArrayList<>();
//        data.forEach((table) -> {
//            boolean newSheet = false;
//            Sheet sheet;
//            if (table == null) {
//                sheet = wb.getSheetAt(0);
//            } else if (tables.contains(table)) {
//                sheet = wb.getSheet(table);
//            } else if (newFile) {
//                sheet = wb.getSheetAt(0);
//                tables.remove(sheet.getSheetName());
//                sheet.getWorkbook().setSheetName(sheet.getWorkbook().getSheetIndex(sheet), table);
//            } else {
//                sheet = wb.createSheet(table);
//                tables.add(table);
//                newSheet = true;
//            }
//
//            if (newFile || newSheet) {
//                List<String> title = getTitle(data, before(), after());
//                if (title != null) {
//                    Row titleRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
//                    title.forEach(t -> titleRow.createCell(titleRow.getPhysicalNumberOfCells()).setCellValue(t));
//                }
//            }
//
//            Float colHeight = null;
//            List<CellStyle> rowStyles = null;
//            if (newFile || newSheet) {
//                if (this.colHeight != null || followStyles || style != null || _data.size() > 1) {
//                    wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
//                }
//            }
//
//            if (newFile || newSheet) {
//                if (this.colHeight != null || followStyles) {
//                    int lastRowNum = sheet.getLastRowNum();
//                    Row lastRow = sheet.getRow(lastRowNum);
//                    if (lastRow != null) {
//                        colHeight = lastRow.getHeightInPoints();
//                        if (followStyles) {
//                            rowStyles = new ArrayList<>();
//                            for (Cell cell : lastRow) {
//                                rowStyles.add(new CellBase(cell.getCellStyle()));
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (newFile || newSheet) {
//                if (fitHead && _head.get(sheet.getSheetName()) == null) {
//                    _head.put(sheet.getSheetName(), getTitle(data.get(0), _before, _after));
//                }
//            }
//
//            if (fitHead && _head.get(sheet.getSheetName()) != null) {
//                data.forEach(d -> {
//                    if (d instanceof Map) {
//                        d = ((Map<?, ?>) d).values();
//                    }
//                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//                    _head.get(sheet.getSheetName()).forEach(h -> row.createCell(row.getPhysicalNumberOfCells()).setCellValue(processContent(((Map<?, ?>) d).get(h))));
//                    setStyle(colHeight, rowStyles, row);
//                });
//            } else {
//                data.forEach(d -> {
//                    if (d instanceof Map) {
//                        d = ((Map<?, ?>) d).values();
//                    }
//                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//                    ((List<?>) d).forEach(value -> row.createCell(row.getPhysicalNumberOfCells()).setCellValue(processContent(value)));
//                    setStyle(colHeight, rowStyles, row);
//                });
//            }
//        });
//
//        try {
//            wb.write(filePath.toFile());
//            wb.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void setStyle(Float colHeight, List<CellStyleCopier> rowStyles, Row row) {
//        if (colHeight != null) {
//            row.setHeightInPoints(colHeight);
//        }
//
//        if (rowStyles != null) {
//            for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
//                Cell cell = row.getCell(i);
//                CellStyleCopier styleCopier = rowStyles.get(i);
//                styleCopier.setToCell(cell);
//            }
//        } else if (style != null) {
//            for (Cell cell : row) {
//                setStyle(style, cell);
//            }
//        }
//    }
//
//    protected void setStyle(String style, Table.Cell cell) {
//        // 实现设置样式的逻辑
//    }
//
//    protected List<String> getTitle(Object data, Object before, Object after) {
//        if (data instanceof List) {
//            return null;
//        }
//
//        List<String> returnList = new ArrayList<>();
//        List<String> beforeList = getList(before);
//        List<String> afterList = getList(after);
//
//        for (Object obj : List.of(beforeList, data, afterList)) {
//            if (obj instanceof Map) {
//                returnList.addAll(((Map<?, ?>) obj).keySet());
//            } else if (obj == null) {
//                // Do nothing
//            } else if (obj instanceof List) {
//                ((List<?>) obj).forEach(o -> returnList.add(""));
//            } else {
//                returnList.add("");
//            }
//        }
//
//        return returnList;
//    }
//
//    protected List<String> getList(Object obj) {
//        if (obj instanceof List) {
//            return (List<String>) obj;
//        } else if (obj instanceof Map) {
//            return new ArrayList<>(((Map<?, ?>) obj).keySet());
//        } else {
//            return List.of("");
//        }
//    }

    // 其他方法和属性的具体实现
}