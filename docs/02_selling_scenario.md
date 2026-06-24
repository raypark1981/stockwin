# 매도 시나리오 1 - 시초가 매수 후 당일 전량 매도

## 1. 기본 목표

목표:
장전 동시호가에서 시초가 기준으로 매수 체결된 종목을
당일 안에 전량 매도한다.

전략 방향:
- 수익 구간에서는 +3% ~ +5% 목표가에 매도 주문 등록
- 매도 주문 등록 후 실시간 모니터링 시작
- 주가가 시초가 대비 -5%까지 하락하면 손실 제한을 위해 전량 매도
- 당일 보유 종료를 목표로 한다


## 2. 매수 체결 직후 처리

조건:
09:00 시초가 매수 체결 완료

처리:
1. 체결 수량 확인
2. 평균 매수가 확인
3. 시초가 저장
4. 매도 목표가 계산
5. 전량 매도 주문 등록

예시:
시초가: 100,000원
체결 수량: 10주

1차 목표 매도가:
+3% = 103,000원

2차 목표 매도가:
+5% = 105,000원

기본 매도 주문:
지정가 103,000원 전량 매도 주문 등록

또는 공격적으로:
지정가 105,000원 전량 매도 주문 등록


## 3. 매도 주문 등록 후 모니터링 시작

매도 주문이 들어가면 바로 모니터링 배치를 실행한다.

모니터링 대상:
- 현재가
- 시초가 대비 등락률
- 매도 주문 체결 여부
- 미체결 수량
- 호가 상태
- 거래량
- 급락 여부

호출 주기:
09:00 ~ 09:10 : 3초 ~ 5초마다 확인
09:10 이후 : 5초 ~ 10초마다 확인

처음 10분은 변동성이 크기 때문에 더 짧게 본다.


## 4. 수익 매도 로직

기본 로직:
매수가 대비 +3% 또는 +5%에 도달하면 전량 매도한다.

방법:
매수 체결 직후 미리 목표가 매도 주문을 걸어둔다.

예시:
매수가: 100,000원
목표 수익률: +3%
목표 매도가: 103,000원

주문:
지정가 103,000원 전량 매도

체결되면:
전략 종료


## 5. 손실 제한 로직

조건:
현재가가 시초가 대비 -5% 이하로 하락

예시:
시초가: 100,000원
손절 기준가: 95,000원

현재가가 95,000원 이하가 되면:
기존 수익 목표 매도 주문을 취소한다.
전량 매도 주문으로 정정한다.

보수적인 방식:
기존 매도 주문 취소
시장가 또는 현재 매수호가 기준으로 전량 매도

안전한 방식:
기존 매도 주문 취소
현재 매수 1호가 또는 2호가에 지정가 전량 매도

목표:
수익을 포기하고 당일 리스크를 줄이는 것


## 6. 주문 정정 방식

상황:
기존에 +3% 목표가 매도 주문이 걸려 있음

예시:
매수가: 100,000원
목표 매도 주문: 103,000원
현재가: 95,000원

처리:
1. 기존 103,000원 매도 주문 취소
2. 미체결 수량 확인
3. 남은 수량 전량 매도 주문 등록
4. 주문 체결 여부 확인

주의:
취소 요청 후 바로 새 주문을 넣기 전에
미체결 수량을 다시 확인해야 한다.

이유:
취소 요청 중 일부 체결될 수 있기 때문이다.


## 7. 시나리오 흐름

1. 09:00 시초가 매수 체결 확인
2. 체결가, 체결수량 저장
3. 목표 매도가 계산
4. +3% 또는 +5% 지정가 전량 매도 주문 등록
5. 매도 주문 등록 직후 모니터링 배치 시작
6. 현재가와 시초가 대비 등락률 계속 확인
7. 목표가 매도 체결 시 전략 종료
8. 시초가 대비 -5% 하락 시 기존 매도 주문 취소
9. 남은 수량 전량 매도 주문 등록
10. 체결 확인
11. 당일 장 종료 전 미체결 수량이 남아 있으면 전량 정리


## 8. 의사코드

buyInfo = getBuyExecutionInfo(stockCode)

if buyInfo.isFilled:

    buyPrice = buyInfo.averagePrice
    openPrice = buyInfo.openPrice
    quantity = buyInfo.filledQuantity

    targetRate = 0.03
    targetSellPrice = buyPrice * 1.03

    stopLossPrice = openPrice * 0.95

    sellOrderId = sendLimitSellOrder(
        stockCode = stockCode,
        price = targetSellPrice,
        quantity = quantity
    )

    while marketIsOpen:

        currentPrice = getCurrentPrice(stockCode)
        sellOrderStatus = getOrderStatus(sellOrderId)

        if sellOrderStatus.isAllFilled:
            finishStrategy()
            break

        if currentPrice <= stopLossPrice:

            cancelOrder(sellOrderId)

            remainQuantity = getRemainQuantity(stockCode)

            if remainQuantity > 0:
                sendSellOrder(
                    stockCode = stockCode,
                    price = currentBidPrice,
                    quantity = remainQuantity
                )

            finishStrategy()
            break

        sleep(5초)