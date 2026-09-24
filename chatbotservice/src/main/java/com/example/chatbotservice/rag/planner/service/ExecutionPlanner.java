package com.example.chatbotservice.rag.planner.service;

import com.example.chatbotservice.rag.planner.IntentType;
import com.example.chatbotservice.rag.planner.model.ExecutionPlan;
import com.example.chatbotservice.rag.tool.ToolType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExecutionPlanner {

    public ExecutionPlan createPlan(
            IntentType intent,
            Map<String,Object> params){

        return switch (intent){

            case FLIGHT_SEARCH ->

                    new ExecutionPlan(

                            intent,

                            List.of(ToolType.FLIGHT_SEARCH),

                            params
                    );

            case HOTEL_SEARCH ->

                    new ExecutionPlan(

                            intent,

                            List.of(ToolType.HOTEL_SEARCH),

                            params
                    );

            case BOOK_HOTEL ->

                    new ExecutionPlan(

                            intent,

                            List.of(

                                    ToolType.HOTEL_SEARCH,

                                    ToolType.PRICE_REVALIDATION,

                                    ToolType.HOTEL_BOOK

                            ),

                            params
                    );

            case BOOK_FLIGHT ->

                    new ExecutionPlan(

                            intent,

                            List.of(

                                    ToolType.FLIGHT_SEARCH,

                                    ToolType.PRICE_REVALIDATION

                            ),

                            params
                    );

            case FLIGHT_STATUS ->

                    new ExecutionPlan(

                            intent,

                            List.of(

                                    ToolType.FLIGHT_STATUS

                            ),

                            params
                    );

            case CANCEL_BOOKING ->

                    new ExecutionPlan(

                            intent,

                            List.of(

                                    ToolType.HOTEL_CANCEL

                            ),

                            params
                    );

            default ->

                    new ExecutionPlan(

                            intent,

                            List.of(),

                            params
                    );
        };
    }

}
