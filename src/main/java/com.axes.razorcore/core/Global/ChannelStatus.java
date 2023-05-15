package com.axes.razorcore.core.Global;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChannelStatus
{
    /// <summary>
    /// The channel is empty
    /// </summary>
    public  String Vacated = "channel_vacated";

    /// <summary>
    /// The channel has subscribers
    /// </summary>
    public  String Occupied = "channel_occupied";
}
