package com.example.schedule;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@RequestScoped
@Path("schedule")
public class ScheduleResources {

    @Inject
    NotificationService notificationService;

    @Inject
    StandUpMeeting meeting;

    @POST
    public Response invite() {
        meeting.sendInviteNotifications();
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getInvitedNames() {
        return notificationService.getNames();
    }
}
