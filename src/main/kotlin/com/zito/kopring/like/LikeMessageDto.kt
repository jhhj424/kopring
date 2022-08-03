package com.zito.kopring.like

import io.swagger.annotations.ApiModelProperty

data class LikeMessageDto(
    @ApiModelProperty(value = "redis key")
    val key: String
)
