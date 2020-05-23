package com.techyourchance.testdrivendevelopment.exercise6;

import org.jetbrains.annotations.Nullable;
import com.techyourchance.testdrivendevelopment.exercise6.users.User;

import java.util.Objects;

public interface FetchUserUseCaseSync {

    enum Status {
        SUCCESS,
        FAILURE,
        NETWORK_ERROR
    }

    class UseCaseResult {
        private final Status mStatus;

        @Nullable
        private final User mUser;

        public UseCaseResult(Status status, @Nullable User user) {
            mStatus = status;
            mUser = user;
        }

        public Status getStatus() {
            return mStatus;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UseCaseResult that = (UseCaseResult) o;
            return mStatus == that.mStatus &&
                    Objects.equals(mUser, that.mUser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mStatus, mUser);
        }

        @Nullable
        public User getUser() {
            return mUser;
        }


    }


    UseCaseResult fetchUserSync(String userId);

}
