package com.wallet.service;

import com.wallet.dto.TransferRequest;
import com.wallet.dto.TransactionResponse;
import com.wallet.entity.*;
import com.wallet.entity.enums.*;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.exception.WalletClosedException;
import com.wallet.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        sender = User.builder().id(1L).email("sender@test.com")
                .role(Role.ROLE_USER).fullName("Sender User").build();
        receiver = User.builder().id(2L).email("receiver@test.com")
                .role(Role.ROLE_USER).fullName("Receiver User").build();

        senderWallet = Wallet.builder()
                .id(1L).walletNumber("WLT-SENDER")
                .balance(new BigDecimal("1000.00"))
                .status(WalletStatus.ACTIVE).user(sender).build();

        receiverWallet = Wallet.builder()
                .id(2L).walletNumber("WLT-RECEIVER")
                .balance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE).user(receiver).build();
    }

    @Test
    @DisplayName("Successful transfer: balances updated correctly")
    void transfer_success() {
        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber("WLT-RECEIVER")
                .amount(new BigDecimal("200.00"))
                .note("Test transfer")
                .build();

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(walletRepository.findActiveWalletByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByWalletNumber("WLT-RECEIVER")).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t = Transaction.builder()
                    .id(1L).referenceId("TXN-TEST-001")
                    .senderWallet(senderWallet).receiverWallet(receiverWallet)
                    .amount(request.getAmount()).status(TransactionStatus.SUCCESS)
                    .note(request.getNote()).build();
            return t;
        });

        TransactionResponse response = transactionService.transfer(request, "sender@test.com");

        assertThat(response).isNotNull();
        assertThat(senderWallet.getBalance()).isEqualByComparingTo("800.00");
        assertThat(receiverWallet.getBalance()).isEqualByComparingTo("700.00");
        verify(notificationService).sendTransactionSuccessNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Transfer fails: insufficient balance")
    void transfer_insufficientBalance_throwsException() {
        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber("WLT-RECEIVER")
                .amount(new BigDecimal("5000.00")) // More than balance
                .build();

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(walletRepository.findActiveWalletByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByWalletNumber("WLT-RECEIVER")).thenReturn(Optional.of(receiverWallet));

        assertThatThrownBy(() -> transactionService.transfer(request, "sender@test.com"))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(transactionRepository, never()).save(any());
        verify(notificationService, never()).sendTransactionSuccessNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Transfer fails: receiver wallet is closed")
    void transfer_closedReceiverWallet_throwsException() {
        receiverWallet.setStatus(WalletStatus.CLOSED);

        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber("WLT-RECEIVER")
                .amount(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(walletRepository.findActiveWalletByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByWalletNumber("WLT-RECEIVER")).thenReturn(Optional.of(receiverWallet));

        assertThatThrownBy(() -> transactionService.transfer(request, "sender@test.com"))
                .isInstanceOf(WalletClosedException.class)
                .hasMessageContaining("Receiver wallet is closed");
    }

    @Test
    @DisplayName("Transfer fails: receiver wallet not found")
    void transfer_receiverNotFound_throwsException() {
        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber("WLT-INVALID")
                .amount(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(walletRepository.findActiveWalletByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByWalletNumber("WLT-INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(request, "sender@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Transfer fails: self-transfer not allowed")
    void transfer_selfTransfer_throwsException() {
        receiverWallet.setWalletNumber("WLT-SENDER"); // Same wallet number

        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber("WLT-SENDER")
                .amount(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(walletRepository.findActiveWalletByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByWalletNumber("WLT-SENDER")).thenReturn(Optional.of(receiverWallet));

        assertThatThrownBy(() -> transactionService.transfer(request, "sender@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own wallet");
    }
}
