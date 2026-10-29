package com.brandonkamga.sourcefolio.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionalServicesTest {

    interface Catalog {
        String findOne();

        void publish();
    }

    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final TransactionalServices services = new TransactionalServices(transactions);

    @Test
    @DisplayName("a method named like a read runs in a read-only transaction")
    void readsAreReadOnly() {
        when(transactions.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        Catalog catalog = services.wrap(Catalog.class, new Catalog() {
            public String findOne() { return "found"; }
            public void publish() { }
        });

        assertThat(catalog.findOne()).isEqualTo("found");

        assertThat(definition().isReadOnly()).isTrue();
    }

    @Test
    @DisplayName("any other method runs read-write and rolls back on a runtime exception")
    void writesRollBackOnFailure() {
        TransactionStatus status = mock(TransactionStatus.class);
        when(transactions.getTransaction(any())).thenReturn(status);
        Catalog catalog = services.wrap(Catalog.class, new Catalog() {
            public String findOne() { return null; }
            public void publish() { throw new IllegalStateException("refused"); }
        });

        assertThatThrownBy(catalog::publish).isInstanceOf(IllegalStateException.class);

        assertThat(definition().isReadOnly()).isFalse();
        verify(transactions).rollback(status);
    }

    private TransactionDefinition definition() {
        ArgumentCaptor<TransactionDefinition> captor = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactions).getTransaction(captor.capture());
        return captor.getValue();
    }
}
