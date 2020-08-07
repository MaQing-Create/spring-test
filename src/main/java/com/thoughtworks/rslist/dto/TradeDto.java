package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "trade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDto {
    @Id
    @GeneratedValue
    private int id;
    private int amount;
    private int rank;
    @OneToOne
    @JoinColumn
    private RsEventDto rsEvent;
}