package dev.aftermoon.indianpoker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.skydoves.elasticviews.ElasticAnimation;
import com.skydoves.elasticviews.ElasticFinishListener;

import java.util.Arrays;
import java.util.Random;

import dev.aftermoon.indianpoker.databinding.ActivityGameBinding;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    // 플레이 인원
    private final int PLAYER_NUM = 2;

    // 실제 사람은 0번째 플레이어
    private final int PLAYER = 0;

    // 컴퓨터는 1번째 플레이어
    private final int COMPUTER = 1;

    // 게임 전체에서 각 카드의 남은 수량을 담는 배열
    private final int[] cardList = new int[10];

    // 플레이어들이 가지고 있는 카드를 담는 배열
    private final int[] playerCard = new int[PLAYER_NUM];

    // 플레이어들의 소지 코인을 담는 배열
    private final int[] playerCoin = new int[PLAYER_NUM];

    // 플레이어들이 베팅한 방법을 담는 배열
    private final int[] playerBetMethod = new int[PLAYER_NUM];

    // 플레이어들의 현재 베팅 코인
    private final int[] playerBetCoin = new int[PLAYER_NUM];

    // 컴퓨터가 확률 계산을 위해 남은 카드의 수량을 담는 배열
    private final int[] computerCardList = new int[10];

    // 현재 게임 내에서의 총 베팅 코인
    private int currentAllBetCoin;

    // 현재 게임 내에서의 남은 카드 수의 갯수
    private int currentRemainCard;

    // 현재 턴 플레이어
    private int currentTurnPlayer;

    // 유저 이름
    private String userName;

    // 베팅 대기 Countdown
    private CountDownTimer countDownTimer;

    // 타이머 일시정지 이전 시간 저장용
    private long currentMillis;

    // 일시정지 상태 확인
    private boolean isPaused = false;

    /** 안드로이드 Override **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 시작 사운드 재생
        EffectSoundManager.getInstance(this).play(R.raw.start);

        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 플레이어 이름 설정
        Bundle bundle = getIntent().getExtras();
        userName = bundle != null ? bundle.getString("name") : "플레이어";
        if(userName == null || userName.isEmpty()) userName = "플레이어";

        binding.tvPlayerName.setText(userName);

        // 버튼 이벤트 설정
        setButtonEvent();

        // 변수 초기화
        resetVariable();

        // 게임 시작
        gameStart(true);
    }

    @Override
    public void onBackPressed() {
        showSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer != null) {
            try {
                countDownTimer.cancel();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        countDownTimer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isBGMOn = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean("isBGMOn", true);
        if(isBGMOn) startService(new Intent(this, BGMService.class));

        if(isPaused) {
            isPaused = false;
            if(currentTurnPlayer == PLAYER) createPlayerTimer();
            else createComputerTimer();
            countDownTimer.start();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        isPaused = true;
        countDownTimer.cancel();
        stopService(new Intent(this, BGMService.class));
    }

    /** UI 관련 **/
    private void setButtonEvent() {
        binding.btnDie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewAnimation(v);
                playerBetMethod[PLAYER] = 0;
                setPlayerBetMethodText();
                EffectSoundManager.getInstance(GameActivity.this).play(R.raw.select);
            }
        });

        binding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewAnimation(v);
                playerBetMethod[PLAYER] = 1;
                setPlayerBetMethodText();
                EffectSoundManager.getInstance(GameActivity.this).play(R.raw.select);
            }
        });

        binding.btnQuarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewAnimation(v);
                playerBetMethod[PLAYER] = 2;
                setPlayerBetMethodText();
                EffectSoundManager.getInstance(GameActivity.this).play(R.raw.select);
            }
        });

        binding.btnHalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewAnimation(v);
                playerBetMethod[PLAYER] = 3;
                setPlayerBetMethodText();
                EffectSoundManager.getInstance(GameActivity.this).play(R.raw.select);
            }
        });

        binding.btnAllin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewAnimation(v);
                playerBetMethod[PLAYER] = 4;
                setPlayerBetMethodText();
                EffectSoundManager.getInstance(GameActivity.this).play(R.raw.select);
            }
        });

        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetting();
            }
        });
    }

    private void showSetting() {
        EffectSoundManager.getInstance(GameActivity.this).play(R.raw.select);

        // 카운터 정지
        countDownTimer.cancel();

        // 다이얼로그 객체 생성 및 클릭 이벤트들 설정
        final SettingDialog settingDialog = new SettingDialog(this);

        // 다이얼로그 보이기
        settingDialog.show();

        // 크기 조절
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Window window = settingDialog.getWindow();

        int x = (int) (size.x * 0.5f);
        int y = (int) (size.y * 0.7f);

        window.setLayout(x, y);

        // SP 로드
        final SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        // 클릭 이벤트 설정
        Button btnGameStop = settingDialog.findViewById(R.id.btn_gamestop);
        btnGameStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingDialog.cancel();
                finish();
            }
        });

        Button btnClose = settingDialog.findViewById(R.id.btn_close) ;
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingDialog.dismiss();
            }
        });


        CheckBox bgmCheckBox = (CheckBox) settingDialog.findViewById(R.id.cb_bgm);
        bgmCheckBox.setChecked(prefs.getBoolean("isBGMOn", true));
        bgmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // BGM
                editor.putBoolean("isBGMOn", isChecked);
                editor.apply();

                if(isChecked) {
                    startService(new Intent(GameActivity.this, BGMService.class));
                }
                else {
                    stopService(new Intent(GameActivity.this, BGMService.class));
                }
            }
        });

        CheckBox effectSoundCheckbox = (CheckBox) settingDialog.findViewById(R.id.cb_effectsound);
        effectSoundCheckbox.setChecked(prefs.getBoolean("isEffectSoundOn", true));
        effectSoundCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 이펙트 사운드
                editor.putBoolean("isEffectSoundOn", isChecked);
                editor.apply();
            }
        });

        // Dialog가 Dismiss되면 다시 CountDown 처리
        settingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(currentTurnPlayer == PLAYER) createPlayerTimer();
                else createComputerTimer();
                countDownTimer.start();
            }
        });
    }

    private void setPlayerBetMethodText() {
        // 플레이어 베팅시 Text 설정
        binding.tvCurrentPlayerbet.setText(getString(R.string.current_betmethod, getBetMethodText(PLAYER), calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER)));
        showViewAnimation(binding.tvCurrentPlayerbet);
    }

    private String getBetMethodText(int player) {
        // 베팅 방법에 따라 String으로 변환
        String betString = "";
        if(playerBetMethod[player] == 0) betString = "다이";
        else if(playerBetMethod[player] == 1) betString = "콜";
        else if(playerBetMethod[player] == 2) betString = "쿼터";
        else if(playerBetMethod[player] == 3) betString = "하프";
        else if(playerBetMethod[player] == 4) betString = "올인";
        return betString;
    }

    private void showViewAnimation(View v) {
        // 버튼 에니메이션 설정
        new ElasticAnimation(v)
                .setScaleX(0.85f)
                .setScaleY(0.85f)
                .setDuration(800)
                .setOnFinishListener(new ElasticFinishListener() {
                    @Override
                    public void onFinished() {

                    }
                })
                .doAction();
    }

    private void enablePlayerButton() {
        // 플레이어가 베팅 가능한 금액인경우 버튼 활성화
        binding.btnDie.setEnabled(calculateBetPrice(0, PLAYER, COMPUTER) >= 0);
        binding.btnCall.setEnabled(calculateBetPrice(1, PLAYER, COMPUTER) >= 0);
        binding.btnQuarter.setEnabled(calculateBetPrice(2, PLAYER, COMPUTER) >= 0);
        binding.btnHalf.setEnabled(calculateBetPrice(3, PLAYER, COMPUTER) >= 0);
        binding.btnAllin.setEnabled(calculateBetPrice(4, PLAYER, COMPUTER) >= 0);
    }

    private void disablePlayerButton() {
        // 플레이어의 모든 버튼 비활성화
        binding.btnDie.setEnabled(false);
        binding.btnCall.setEnabled(false);
        binding.btnQuarter.setEnabled(false);
        binding.btnHalf.setEnabled(false);
        binding.btnAllin.setEnabled(false);
    }

    /** 변수 관련 **/
    private void resetVariable() {
        // 변수 리셋
        resetCard();
        Arrays.fill(playerCard, 0);
        Arrays.fill(playerCoin, 100);
        Arrays.fill(playerBetCoin, 0);
        Arrays.fill(playerBetMethod, 0);
        currentAllBetCoin = 0;
        currentTurnPlayer = 0;

        // 텍스트 새로고침
        refreshTextView();

        // 카드 설정
        binding.ivCompterCard.setImageResource(R.drawable.card_blind);
        binding.ivPlayerCard.setImageResource(R.drawable.card_blind);
    }

    private void resetCard() {
        // 카드 관련 변수 리셋
        currentRemainCard = 20;
        Arrays.fill(cardList, 2);
        Arrays.fill(computerCardList, 2);
    }

    private void refreshTextView() {
        // TextView 새로고침
        binding.tvUserCoin.setText(getString(R.string.current_coin, playerCoin[PLAYER]));
        binding.tvComputerCoin.setText(getString(R.string.current_coin, playerCoin[COMPUTER]));
        binding.tvCurrentPlayerbet.setText(getString(R.string.current_betmethod, getBetMethodText(PLAYER), calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER)));
        binding.tvCurrentCombet.setText(getString(R.string.current_betmethod, getBetMethodText(COMPUTER), calculateBetPrice(playerBetMethod[COMPUTER], COMPUTER, PLAYER)));
        binding.tvCurrentAllbet.setText(getString(R.string.current_betcoin, currentAllBetCoin));
        binding.tvCurrentTurn.setText(getString(R.string.current_turn, currentTurnPlayer == 0 ? userName : "안드로이드"));
    }

    /** 게임 진행 관련 **/
    private int getRandomCard() {
        // 현재 남은 카드가 1장 이상이여야 카드를 줄 수 있음
        if(currentRemainCard >= 1) {
            Random random = new Random();

            // 랜덤하게 1~10에서 하나 선택
            int card = random.nextInt(10) + 1;

            // 만약 해당 Card가 0개 초과라면
            if (cardList[card - 1] > 0) {
                // 전체 카드 중 남은 카드 수 1 제거
                currentRemainCard--;

                // 해당 카드의 남은 수 1 제거
                cardList[card - 1]--;

                // 카드 남은 수 Text 설정
                binding.tvCurrentCard.setText(getString(R.string.current_remaincard, currentRemainCard));

                // 해당 카드를 리턴
                return card;
            }
            // 카드가 없다면 Recursive하게 불러와서 겹치지 않는 카드를 가지도록 함
            else {
                return getRandomCard();
            }
        }
        // 남은 카드가 없다면 리셋하고 다시 호출
        else {
            resetCard();
            return getRandomCard();
        }
    }

    // 일반 게임 시작
    private void gameStart(boolean isResetBet) {
        // Activity가 Destroy 되었다면 Finish and Return
        if(this.isDestroyed()) {
            finish();
            return;
        }

        if(playerCoin[PLAYER] < 1 || playerCoin[COMPUTER] < 1) {
            Toast.makeText(this, "코인이 모자라서 게임을 종료합니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 카드 다시 가리기
        binding.ivPlayerCard.setImageResource(R.drawable.card_blind);
        binding.ivCompterCard.setImageResource(R.drawable.card_blind);

        // 플레이어의 이전 베팅 방법 리셋
        for(int i = 0; i < PLAYER_NUM; i++) {
            playerBetMethod[i] = 0;
        }

        // 베팅 리셋을 하는 경우
        if(isResetBet) {
            for(int i = 0; i < PLAYER_NUM; i++) {
                playerBetCoin[i] = 0;
            }
        }

        // 텍스트 새로고침
        refreshTextView();

        for(int i = 0; i < PLAYER_NUM; i++) {
            // 각 플레이어에게 랜덤 카드 지급
            playerCard[i] = getRandomCard();

            // i가 컴퓨터일경우
            if(i == COMPUTER) {
                Log.d("start", "Computer Card Number : " + playerCard[i]);
                // 카드 이미지 변경
                int id = getResources().getIdentifier("card_" + (playerCard[i]), "drawable", getPackageName());
                binding.ivCompterCard.setImageDrawable(ResourcesCompat.getDrawable(getResources(), id, getTheme()));
            }

            // 참가비 1코인씩 베팅
            playerBet(i, 1);
        }

        // 컴퓨터 계산용 카드 리스트에서 플레이어 카드 제외
        computerCardList[playerCard[PLAYER] - 1]--;

        // 플레이어 턴 시작
        playerTurn();
    }

    private void playerTurn() {
        if(this.isDestroyed() || this.isFinishing()) {
            return;
        }

        // 현재 턴은 PLAYER
        currentTurnPlayer = PLAYER;

        // 효과음 재생
        EffectSoundManager.getInstance(this).play(R.raw.turn);

        // 현재 베팅 방법을 선택할 수 없으면 다이로 변경
        if(calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER) == -1) {
            playerBetMethod[PLAYER] = 0;
        }

        // 텍스트 새로고침
        refreshTextView();

        // 플레이어 버튼 활성화
        enablePlayerButton();

        // 플레이어에게 메시지 출력
        Toast.makeText(this, "당신의 턴입니다! 10초 안에 베팅을 해주세요.", Toast.LENGTH_SHORT).show();

        // 10초 타이머 작동
        currentMillis = 10 * 1000;
        createPlayerTimer();
        countDownTimer.start();
    }

    private void computerTurn() {
        if(this.isDestroyed() || this.isFinishing()) {
            return;
        }

        // 현재 턴은 COMPUTER
        currentTurnPlayer = COMPUTER;

        // 현재 베팅 방법을 선택할 수 없으면 다이로 변경
        if(calculateBetPrice(playerBetMethod[COMPUTER], COMPUTER, PLAYER) == -1) {
            playerBetMethod[COMPUTER] = 0;
        }

        // 텍스트 새로고침
        refreshTextView();

        // 플레이어에게 메시지 출력
        Toast.makeText(this, "안드로이드의 턴입니다!", Toast.LENGTH_SHORT).show();

        // 현재 턴 플레이어 이름 변경
        binding.tvCurrentTurn.setText(getString(R.string.current_turn, "안드로이드"));

        // 5초 타이머 작동\
        currentMillis = 5 * 1000;
        createComputerTimer();
        countDownTimer.start();
    }

    private int getWinner() {
        // 둘 다 다이했으면 무승부 (-1)
        if(playerBetMethod[PLAYER] == 0 && playerBetMethod[COMPUTER] == 0) {
            return -1;
        }
        // 둘 중에 한 명이라도 다이를 했으면 우승자는 자동으로 다이 안한 반대쪽 사람
        else if(playerBetMethod[PLAYER] == 0) {
            return COMPUTER;
        }
        else if(playerBetMethod[COMPUTER] == 0) {
            return PLAYER;
        }
        // 다이가 아닌 경우
        else {
            // 플레이어의 카드가 더 크면 플레이어 승리
            if(playerCard[PLAYER] > playerCard[COMPUTER]) {
                return PLAYER;
            }
            // 플레이어의 카드가 더 작으면 컴퓨터 승리
            else if(playerCard[PLAYER] < playerCard[COMPUTER]) {
                return COMPUTER;
            }
            // 둘이 같으면 무승부
            else {
                return -1;
            }
        }
    }

    private void cardOpen() {
        // 플레이어 카드 오픈
        Log.d("cardopen", "player card is " + playerCard[PLAYER]);
        int playerCardID = getResources().getIdentifier("card_" + (playerCard[PLAYER]), "drawable", getPackageName());
        binding.ivPlayerCard.setImageDrawable(ResourcesCompat.getDrawable(getResources(), playerCardID, getTheme()));

        // 카드에 애니메이션
        showViewAnimation(binding.ivPlayerCard);

        // 컴퓨터의 계산용 카드 목록에 본인 카드 제거
        computerCardList[playerCard[COMPUTER]-1]--;
    }

    private void gameResult() {
        if(this.isDestroyed() || this.isFinishing()) return;
        String dialogText = "";

        // 두 명이 모두 올인을 하고, 카드가 같은 경우
        if((playerBetMethod[PLAYER] == 4 && playerBetMethod[COMPUTER] == 4) && (playerCard[PLAYER] == playerCard[COMPUTER])) {
            Toast.makeText(this, "두 카드 수가 같습니다! 승자를 결정하기 위해 카드를 다시 뽑습니다!", Toast.LENGTH_SHORT).show();

            // 카드를 초기화하고 카드 재지급
            resetCard();
            playerCard[PLAYER] = getRandomCard();
            playerCard[COMPUTER] = getRandomCard();
            computerCardList[playerCard[PLAYER] - 1]--;

            // 게임 결과 다시 호출
            gameResult();
        }
        // 둘 중에 한 명이라도 돈이 없거나 콜/다이를 한 경우
        else if((playerCoin[PLAYER] == 0 || playerCoin[COMPUTER] == 0) || (playerBetMethod[PLAYER] == 0 || playerBetMethod[PLAYER] == 1) || (playerBetMethod[COMPUTER] == 0 || playerBetMethod[COMPUTER] == 1)) {
            // 카드 공개
            cardOpen();

            // 승자 확인
            final int winner = getWinner();

            // 승리 플레이어에 따라 Dialog 내용 변경하기
            if(winner == PLAYER) {
                dialogText = "승리하셨습니다!";

                // 효과음 재생
                EffectSoundManager.getInstance(this).play(R.raw.win);
            }
            else if(winner == COMPUTER) {
                dialogText = "패배했습니다!";

                // 효과음 재생
                EffectSoundManager.getInstance(this).play(R.raw.lose);
            }
            else {
                dialogText = "무승부입니다! 현재 베팅 (" +  currentAllBetCoin  + "코인) 이 유지됩니다!";
            }

            // 카드 10인데 다이하면 10원씩 깎임
            if(playerBetMethod[COMPUTER] == 0 && playerCard[COMPUTER] == 10) {
                Toast.makeText(this, "상대가 카드 10을 가지고 다이를 했기 때문에 10코인을 받았습니다!", Toast.LENGTH_SHORT).show();
                playerCoin[COMPUTER] -= 10;
                playerCoin[PLAYER] += 10;
            }

            if(playerBetMethod[PLAYER] == 0 && playerCard[PLAYER] == 10) {
                Toast.makeText(this, "카드 10을 가지고 다이를 했기 때문에 10코인이 감소됩니다.", Toast.LENGTH_SHORT).show();
                playerCoin[PLAYER] -= 10;
                playerCoin[COMPUTER] += 10;
            }

            // 무승부가 아니라면
            if(winner != -1) {
                // 승자에게 현재 베팅된 코인 지급
                playerCoin[winner] += currentAllBetCoin;

                // 여태까지 베팅된 코인 초기화
                currentAllBetCoin = 0;
            }

            // 텍스트 새로고침
            refreshTextView();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(dialogText)
                    .setCancelable(false)
                    .setNegativeButton("나가기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            // 코인이 1개라도 있다면 계속하기 버튼 출력
            if(playerCoin[PLAYER] > 0 && playerCoin[COMPUTER] > 0) {
                dialogBuilder.setMessage(dialogText + "\n게임을 이어서 하시겠습니까?")
                        .setPositiveButton("이어하기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gameStart(winner != -1);
                            }
                        });
            }

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
        // 그 이외에는 베팅 계속 진행
        else {
            continueGame();
        }
    }

    private void continueGame() {
        if(playerCoin[PLAYER] == 0 || playerCoin[COMPUTER] == 0) gameResult();
        playerTurn();
    }


    /** 베팅 계산 **/
    private int calculateComputer() {
        int pcBetMethod = -1;
        int winN = 0;
        int loseN = 0;

        // 플레이어의 현재 카드를 경계로 나누어서 내가 이길 카드를 가지고 있는지 혹은 지는 카드를 가지고 있는지 확률 계산
        for (int i = 0; i < 10; i++) {
            if (i < playerCard[PLAYER] - 1) {
                loseN = loseN + computerCardList[i];
            } else if (i > playerCard[PLAYER] - 1) {
                winN = winN + computerCardList[i];
            }
        }

        // 상대방의 베팅에 따른 확률 조정
        double plusValue = 0.0;
        if(playerBetMethod[PLAYER] == 4) plusValue = -1.0;
        else if(playerBetMethod[PLAYER] == 3) plusValue = -0.5;
        else if(playerBetMethod[PLAYER] == 2) plusValue = -0.25;
        else if(playerBetMethod[PLAYER] == 1) plusValue = 0.25;
        else if(playerBetMethod[PLAYER] == 0) plusValue = 10.0;

        // 이길 확률 계산
        double winP = (((float) winN / (winN + loseN) * 100) + plusValue);

        // 이길 확률이 50% 이하면 다이
        if (winP < 50) pcBetMethod = 0;
        // 이길 확률이 100%이거나 PC가 가진 돈이 전체 판돈 이하일경우 올인
        else if(winP == 100 || playerCoin[COMPUTER] <= currentAllBetCoin) pcBetMethod = 4;
        // 이길 확률이 90% 이상이면서 PC가 가진 돈이 플레이어의 이전 판돈 + 현재 총 베팅금액의 절반 이상일 경우 하프
        else if(winP >= 90 && playerCoin[COMPUTER] >= calculateBetPrice(3, COMPUTER, PLAYER)) pcBetMethod = 3;
        // 이길 확률이 80% 이상이면서 PC가 가진 돈이 플레이어의 이전 판돈 + 현재 총 베팅금액의 4분의 1 이상일 경우 쿼터
        else if(winP >= 80 && playerCoin[COMPUTER] >= calculateBetPrice(2, COMPUTER, PLAYER)) pcBetMethod = 2;
        // 그 이외의 경우에는 콜
        else pcBetMethod = 1;

        return pcBetMethod;
    }

    private boolean playerBet(int playerNum, int betCoin) {
        // Player가 가진 코인 수가 베팅을 원하는 코인 수보다 같거나 많다면
        if(playerCoin[playerNum] >= betCoin) {
            // 베팅 적용
            playerCoin[playerNum] -= betCoin;
            playerBetCoin[playerNum] += betCoin;
            currentAllBetCoin += betCoin;

            // 텍스트 설정
            refreshTextView();

            // 베팅 성공했으므로 true 리턴
            return true;
        }
        // Player가 가진 코인 수가 베팅을 원하는 코인 수보다 적다면 베팅 실패이므로 false 리턴
        else return false;
    }

    private int calculateBetPrice(int method, int player, int player2) {
        // 콜
        if(method == 1) {
            if(playerBetCoin[player2] <= playerCoin[player]) return playerBetCoin[player2];
            else return -1;
        }
        // 쿼터
        else if(method == 2) {
            int qPrice = playerBetCoin[player2] + ((playerBetCoin[player2] + currentAllBetCoin) / 4);
            if(qPrice <= playerCoin[player]) return qPrice;
            else return -1;
        }
        // 하프
        else if(method == 3) {
            int hPrice = playerBetCoin[player2] + ((playerBetCoin[player2] + currentAllBetCoin) / 2);
            if(hPrice <= playerCoin[player]) return hPrice;
            else return -1;
        }
        // 올인
        else if(method == 4) {
            return playerCoin[player];
        }
        else return 0;
    }

    /** 플레이어 타이머 생성 **/
    private void createPlayerTimer() {
        countDownTimer = new CountDownTimer(currentMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 초당 변경
                currentMillis = millisUntilFinished;
                binding.tvRemainingTime.setText(getString(R.string.remain_time, (int) (millisUntilFinished / 1000) % 60));
            }

            @Override
            public void onFinish() {
                // 플레이어 버튼 비활성화
                disablePlayerButton();

                // 베팅
                playerBet(PLAYER, calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER));

                // 텍스트 새로고침
                refreshTextView();

                // 컴퓨터 턴으로 넘어감
                computerTurn();
            }
        };
    }

    /** 컴퓨터 타이머 생성 **/
    private void createComputerTimer() {
        countDownTimer = new CountDownTimer(currentMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentMillis = millisUntilFinished;
                binding.tvRemainingTime.setText(getString(R.string.remain_time, (int) (millisUntilFinished / 1000) % 60));
            }

            @Override
            public void onFinish() {
                // 컴퓨터 베팅 방법 설정
                playerBetMethod[COMPUTER] = calculateComputer();
                Toast.makeText(GameActivity.this, "안드로이드는 " + getBetMethodText(COMPUTER) + "(을)를 선택했습니다!", Toast.LENGTH_SHORT).show();

                // 베팅
                playerBet(COMPUTER, calculateBetPrice(playerBetMethod[COMPUTER], COMPUTER, PLAYER));

                // 텍스트 새로고침
                refreshTextView();

                // 베팅 텍스트 애니메이션
                showViewAnimation(binding.tvCurrentCombet);

                // 게임 결과 로드
                gameResult();
            }
        };
    }
}
