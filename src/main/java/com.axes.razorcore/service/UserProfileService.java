package com.axes.razorcore.service;

import com.axes.razorcore.core.UserProfile;
import com.axes.razorcore.core.UserStatus;
import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.utils.HashingUtils;
import com.axes.razorcore.utils.SerializationUtils;
import com.axes.razorcore.utils.StateHash;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

@Slf4j
public class UserProfileService implements WriteBytesMarshallable, StateHash {

    @Getter
    private final LongObjectHashMap<UserProfile> userProfiles;

    public UserProfileService(BytesIn bytes) {
        this.userProfiles = SerializationUtils.readLongHashMap(bytes, UserProfile::new);
    }

    public UserProfile getUserProfile(long uuid) {
        return userProfiles.get(uuid);
    }

    public UserProfile getUserProfileOrAddSuspended(long uid) {
        return userProfiles.getIfAbsentPut(uid, () -> new UserProfile(uid, UserStatus.SUSPENDED));
    }

    public CommandResultCode balanceAdjustment(final long uid, final int currency, final long amount, final long fundingTransactionId) {

        final UserProfile userProfile = getUserProfile(uid);
        if (userProfile == null) {
            return CommandResultCode.AUTH_INVALID_USER;
        }

        if (userProfile.adjustmentsCounter == fundingTransactionId) {
            return CommandResultCode.USER_MANAGEMENT_ACCOUNT_BALANCE_ADJUSTMENT_ALREADY_APPLIED_SAME;
        }
        if (userProfile.adjustmentsCounter > fundingTransactionId) {
            return CommandResultCode.USER_MANAGEMENT_ACCOUNT_BALANCE_ADJUSTMENT_ALREADY_APPLIED_MANY;
        }

        // validate balance for withdrawals
        if (amount < 0 && (userProfile.accounts.get(currency) + amount < 0)) {
            return CommandResultCode.USER_MANAGEMENT_ACCOUNT_BALANCE_ADJUSTMENT_NSF;
        }

        userProfile.adjustmentsCounter = fundingTransactionId;
        userProfile.accounts.addToValue(currency, amount);

        return CommandResultCode.SUCCESS;
    }

    public boolean addEmptyUserProfile(long uid) {
        if (userProfiles.get(uid) == null) {
            userProfiles.put(uid, new UserProfile(uid, UserStatus.ACTIVE));
            return true;
        } else {
            log.debug("Can not add user, already exists: {}", uid);
            return false;
        }
    }
    public CommandResultCode suspendUserProfile(long uid) {
        final UserProfile userProfile = userProfiles.get(uid);
        if (userProfile == null) {
            return CommandResultCode.USER_MANAGEMENT_USER_NOT_FOUND;

        } else if (userProfile.userStatus == UserStatus.SUSPENDED) {
            return CommandResultCode.USER_MANAGEMENT_USER_ALREADY_SUSPENDED;

        } else if (userProfile.userPositions.anySatisfy(pos -> !pos.isEmpty())) {
            return CommandResultCode.USER_MANAGEMENT_USER_NOT_SUSPENDABLE_HAS_POSITION;

        } else if (userProfile.accounts.anySatisfy(acc -> acc != 0)) {
            return CommandResultCode.USER_MANAGEMENT_USER_NOT_SUSPENDABLE_NON_EMPTY_ACCOUNTS;

        } else {
            log.debug("Suspended user profile: {}", userProfile);
            userProfiles.remove(uid);
            // TODO pool UserProfile objects
            return CommandResultCode.SUCCESS;
        }
    }

    public CommandResultCode resumeUserProfile(long uid) {
        final UserProfile userProfile = userProfiles.get(uid);
        if (userProfile == null) {
            // create new empty user profile
            // account balance adjustments should be applied later
            userProfiles.put(uid, new UserProfile(uid, UserStatus.ACTIVE));
            return CommandResultCode.SUCCESS;
        } else if (userProfile.userStatus != UserStatus.SUSPENDED) {
            // attempt to resume non-suspended account (or resume twice)
            return CommandResultCode.USER_MANAGEMENT_USER_NOT_SUSPENDED;
        } else {
            // resume existing suspended profile (can contain non empty positions or accounts)
            userProfile.userStatus = UserStatus.ACTIVE;
            return CommandResultCode.SUCCESS;
        }
    }
    public void reset() {
        userProfiles.clear();
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        SerializationUtils.marshallLongHashMap(userProfiles, bytes);
    }

    @Override
    public int stateHash() {
        return HashingUtils.stateHash(userProfiles);
    }
}
