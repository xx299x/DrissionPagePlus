## class Select()

Select 类专门用于处理 d 模式下 select 标签。

参数说明：

- ele: select 元素对象

## is_multi

返回：是否多选列表

## options

返回：所有被选中的option元素列表

## selected_option

返回：第一个被选中的option元素

## selected_options

返回：所有被选中的option元素列表

## clear()

清除所有已选项。

## select()

选定或取消选定下拉列表中子元素。

参数说明：

- text_value_index：根据文本、值选或序号择选项，若允许多选，传入list或tuple可多选
- para_type：参数类型，可选 'text'、'value'、'index'
- deselect：是否取消选择

返回：是否选择成功

## select_multi()

选定或取消选定下拉列表中多个子元素。

参数说明：

- text_value_index：根据文本、值选或序号择选多项
- para_type：参数类型，可选 'text'、'value'、'index'
- deselect：是否取消选择

返回：是否选择成功

## deselect()

选定或取消选定下拉列表中子元素。

参数说明：

- text_value_index：根据文本、值选或序号取消择选项，若允许多选，传入list或tuple可多选
- para_type：参数类型，可选 'text'、'value'、'index'

返回：是否选择成功

## deselect_multi()

选定或取消选定下拉列表中多个子元素。

参数说明：

- text_value_index：根据文本、值选或序号取消择选多项
- para_type：参数类型，可选 'text'、'value'、'index'

返回：是否选择成功

## invert()

反选。

