package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.tools.CommonMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
    RsService rsService;

    @Mock
    RsEventRepository rsEventRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VoteRepository voteRepository;
    @Mock
    TradeRepository tradeRepository;
    @Mock
    CommonMethod commonMethod;
    LocalDateTime localDateTime;
    Vote vote;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rsService = new RsService(rsEventRepository, userRepository, voteRepository, commonMethod, tradeRepository);
        localDateTime = LocalDateTime.now();
        vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    }

    @Test
    void shouldVoteSuccess() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        // when
        rsService.vote(vote, 1);
        // then
        verify(voteRepository)
                .save(
                        VoteDto.builder()
                                .num(2)
                                .localDateTime(localDateTime)
                                .user(userDto)
                                .rsEvent(rsEventDto)
                                .build());
        verify(userRepository).save(userDto);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldThrowExceptionWhenUserNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        //when&then
        assertThrows(
                RuntimeException.class,
                () -> {
                    rsService.vote(vote, 1);
                });
    }

    @Test
    void shouldBuyRsSuccess() {
        Trade trade = Trade.builder().amount(10).rank(1).build();
        int id = 1;

        RsEventDto rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第一条事件").build();
        List<RsEventDto> rsEventDtos = new ArrayList<>();
        rsEventDtos.add(rsEventDto);
        TradeDto tradeDto =
                TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEvent(rsEventDto).build();
        when(rsEventRepository.findAll()).thenReturn(rsEventDtos);
        when(commonMethod.getElementFromList(id, rsEventDtos)).thenReturn(rsEventDto);
        when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.empty());

        rsService.buy(trade, id);

        verify(tradeRepository).save(tradeDto);
    }

    @Test
    void shouldThrowExceptionWhenRankNotValid() {
        Trade trade = Trade.builder().amount(10).rank(2).build();
        int id = 1;

        RsEventDto rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第一条事件").build();
        List<RsEventDto> rsEventDtos = new ArrayList<>();
        rsEventDtos.add(rsEventDto);
        TradeDto tradeDto =
                TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEvent(rsEventDto).build();
        when(rsEventRepository.findAll()).thenReturn(rsEventDtos);
        when(commonMethod.getElementFromList(id, rsEventDtos)).thenReturn(rsEventDto);
        when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.empty());


        assertThrows(RequestNotValidException.class, ()->{
            rsService.buy(trade, id);
        });
    }

    @Test
    void shouldThrowExceptionWhenMoneyNotEnough() {
        Trade trade = Trade.builder().amount(10).rank(1).build();
        int id = 1;

        RsEventDto rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第一条事件").build();
        RsEventDto rsEventDtoAtRank = RsEventDto.builder().keyword("无分类").eventName("第一条事件").price(15).build();
        List<RsEventDto> rsEventDtos = new ArrayList<>();
        rsEventDtos.add(rsEventDto);
        TradeDto tradeDto =
                TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEvent(rsEventDto).build();
        when(rsEventRepository.findAll()).thenReturn(rsEventDtos);
        when(commonMethod.getElementFromList(id, rsEventDtos)).thenReturn(rsEventDto);
        when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.of(rsEventDtoAtRank));


        assertThrows(RequestNotValidException.class, ()->{
            rsService.buy(trade, id);
        });
    }

    @Test
    void shouldDeleteReplacedRs() {
        Trade trade = Trade.builder().amount(10).rank(1).build();
        int id = 1;

        RsEventDto rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第一条事件").build();
        RsEventDto rsEventDtoAtRank =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).price(5).id(1).build();
        List<RsEventDto> rsEventDtos = new ArrayList<>();
        rsEventDtos.add(rsEventDto);
        TradeDto tradeDto =
                TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEvent(rsEventDto).build();
        when(rsEventRepository.findAll()).thenReturn(rsEventDtos);
        when(commonMethod.getElementFromList(id, rsEventDtos)).thenReturn(rsEventDto);
        when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.of(rsEventDtoAtRank));

        rsService.buy(trade, id);

        verify(rsEventRepository).deleteById(rsEventDtoAtRank.getId());
    }
}
