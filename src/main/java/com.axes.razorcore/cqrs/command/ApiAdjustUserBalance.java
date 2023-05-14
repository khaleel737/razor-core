/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axes.razorcore.cqrs.command;


import com.axes.razorcore.core.BalanceAdjustedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public final class ApiAdjustUserBalance extends ApiCommand {

    public final long uuid;

    public final int currency;
    public final long amount;
    public final long transactionId;

    public final BalanceAdjustedType adjustmentType = BalanceAdjustedType.ADJUSTED; // TODO support suspend

    @Override
    public String toString() {
        String amountFmt = String.format("%s%d c%d", amount >= 0 ? "+" : "-", Math.abs(amount), currency);
        return "[ADJUST_BALANCE " + uuid + " id:" + transactionId + " " + amountFmt + " " + adjustmentType + "]";

    }
}
