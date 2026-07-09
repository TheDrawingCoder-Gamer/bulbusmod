package gay.menkissing.bulbus.api;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;


/**
 * A storage-like interface for storages who don't need a discriminator between different types,
 * because they only have one possible stored type.
 * For types that _do_ need a discriminator, see fabricmc's builtin storage interface.
 */
public interface SingleTypeStorage {
    default boolean supportsInsertion() {
        return true;
    }
    
    long insert(long maxAmount, TransactionContext transaction);
    
    default boolean supportsExtraction() {
        return true;
    }
    
    long extract(long maxAmount, TransactionContext transaction);
    
    long getAmount();
    
    long getCapacity();
    
}
