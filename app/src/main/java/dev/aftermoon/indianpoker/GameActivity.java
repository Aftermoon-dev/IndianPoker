package dev.aftermoon.indianpoker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import java.util.Arrays;
import java.util.Random;

import dev.aftermoon.indianpoker.databinding.ActivityGameBinding;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    // 모든 유저의 현재 소지 금액을 저장할 변수
    private int[] userCoin;

    // 모든 유저의 현재 카드를 저장할 변수
    private int[] userCard;

    // 컴퓨터가 확률을 계산할 때 사용할 현재까지 나온 카드 갯수
    private int[][] useCard;

    // 현재 남은 각 카드 수
    private int[] currentCard;

    // 전체 카드 중 남은 수
    private int currentCardCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Activity 들어올 땐 System UI 숨기기
        Util.hideSystemUI(getWindow());

        // 모든 변수 초기화
        resetVariable();
    }

    @Override
    protected void onDestroy() {
        // Activity 나갈땐 System UI 다시 나타내기
        Util.showSystemUI(getWindow());
        super.onDestroy();
    }

    /*
     * 플레이어에게 Random한 카드를 지급한다
     * @author Minjae Seon
     * @return 가지고 있을 카드 넘버 (-1 = 카드가 없음)
     */
    private int giveRandomCard() {
        // 현재 남은 카드가 1장 이상일 경우
        if(currentCardCnt > 0) {
            // 랜덤으로 1~10중 하나 뽑기
            Random random = new Random();
            int cardNum = random.nextInt(10) + 1;

            // 해당 카드의 잔여 카드 수가 1장 이상인경우
            if (currentCard[cardNum - 1] > 0) {
                // 해당 카드의 잔여 수 1장 제거
                currentCard[cardNum - 1]--;
                currentCardCnt--;

                // 해당 카드 숫자 리턴
                return cardNum;
            }
            // 해당 카드의 잔여 수가 없는 경우
            else {
                // 재귀 호출로 다른 카드를 정하도록 함
                return giveRandomCard();
            }
        }
        return -1;
    }

    /*
     * 모든 변수 초기화
     * @author Minjae Seon
     */
    private void resetVariable() {
        // userCoin과 userCard에서는 무조건 0번째가 실제 Player의 값

        // user들이 소지하고 있는 코인
        userCoin = new int[4];

        // user들이 현재 소지하고 있는 카드
        // userCard[0]이 게임이 끝나기 전까지는 UI에 노출되서는 안됨 (플레이어의 카드)
        userCard = new int[4];

        // useCard는 현재까지 나온 카드 (컴퓨터가 확률 계산시 이용)
        useCard = new int[4][10];
        for(int i = 0; i < 10; i++) {
            useCard[0][i] = 2;
        }

        // 현재 남은 카드 수 배열
        currentCard = new int[10];
        // 모두 2장씩 있으므로 2로 초기화
        Arrays.fill(currentCard, 2);

        // 현재 남은 카드 수 배열
        currentCardCnt = 20;
    }

    /*
     * Computer의 베팅 방법 결정 계산 Method
     * @param playerNumber 계산을 할 기준 플레이어 번호
     * @author Minjae Seon, Hyeonho Shin
     */
    private int calculate(int playerNumber) {
        int betMethod = -1;

        return betMethod;
    }
}