package com.example.provider.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Producer {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String data;
    private String topic;
    private Integer usedPartition;
    private String offset;
    private String time;
}
