package com.thoughtworks.rslist.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RsEvent implements Serializable {
    @NotNull
    private String eventName;
    @NotNull
    private String keyword;
    @Builder.Default
    private int voteNum = 0;
    @NotNull
    private int userId;
    @Builder.Default
    private int rank = 0;
    @Builder.Default
    private int price = 0;
}
