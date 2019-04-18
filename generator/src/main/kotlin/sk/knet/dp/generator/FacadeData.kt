package sk.knet.dp.generator

import sk.knet.dp.petriflow.DataType
import sk.knet.dp.petriflow.Logic


class FacadeData(
        var id: String,
        var logic: Logic,
        var type: DataType,
        var values: List<String>? = null
)
