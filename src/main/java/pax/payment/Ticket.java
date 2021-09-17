//package pax.payment;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//
//@Data
//public class Ticket {
//
//    /**
//     * Ticket
//     */
//    @JsonProperty("omnivore_tickets_id")
//    private String omnivoreTicketId;
//    @JsonProperty("location_id")
//    private String locationId;
//    @JsonProperty("omnivore_location_id")
//    private String omnivoreLocationId;
//    @JsonProperty("ticket_id")
//    private String ticketId;
//    @JsonProperty("v1_id")
//    private String v1Id;
//    @JsonProperty("pos_id")
//    private String posId;
//    @JsonProperty("auto_send")
//    private String autoSend;
//    @JsonProperty("closed_at")
//    private String closedAt;
//    @JsonProperty("guest_count")
//    private String guestCount;
//    @JsonProperty("name")
//    private String name;
//    @JsonProperty("open")
//    private String open;
//    @JsonProperty("opened_at")
//    private String openedAt;
//    @JsonProperty("voided")
//    private String voided;
//    @JsonProperty("ticket_number")
//    private String ticketNumber;
//    @JsonProperty("split_number")
//    private String splitNumber;
//    @JsonProperty("sub_total")
//    private String subtotal;
//    @JsonProperty("service_charges")
//    private String serviceCharges;
//    @JsonProperty("tax")
//    private String tax;
//    @JsonProperty("total")
//    private String total;
//    @JsonProperty("other_charges")
//    private String otherCharges;
//    @JsonProperty("grand_total")
//    private String grandTotal;
//    @JsonProperty("payment")
//    private String payment;
//    @JsonProperty("clover_payment")
//    private String cloverPayment;
//    @JsonProperty("due")
//    private String due;
//    @JsonProperty("enbedded_employee_check_name") // Misspelling in API
//    private String embeddedEmployeeCheckName;
//    @JsonProperty("enbedded_employee_first_name") // Misspelling in API
//    private String embeddedEmployeeFirstName;
//    @JsonProperty("enbedded_employee_id")
//    private String embeddedEmployeeId;
//    @JsonProperty("enbedded_employee_last_name")
//    private String embeddedEmployeeLastName;
//    @JsonProperty("enbedded_employee_login")
//    private String embeddedEmployeeLogin;
//    @JsonProperty("embedded_order_type_available")
//    private String embeddedOrderTypeAvailable;
//    @JsonProperty("embedded_order_type_id")
//    private String embeddedOrderTypeId;
//    @JsonProperty("embedded_order_type_name")
//    private String embeddedOrderTypeName;
//    @JsonProperty("embedded_revenue_centers_default")
//    private String embeddedRevenueCentersDefault;
//    @JsonProperty("embedded_revenue_centers_id")
//    private String embeddedRevenueCentersId;
//    @JsonProperty("embedded_revenue_centers_name")
//    private String embeddedRevenueCentersName;
//    @JsonProperty("embedded_table_available")
//    private String embeddedTableAvailable;
//    @JsonProperty("embedded_table_id")
//    private String embeddedTableId;
//    @JsonProperty("embedded_table_name")
//    private String embeddedTableName;
//    @JsonProperty("embedded_table_number")
//    private String embeddedTableNumber;
//    @JsonProperty("embedded_table_seats")
//    private String embeddedTableSeats;
//    @JsonProperty("aireus_tray")
//    private String aireusTray;
//    @JsonProperty("enbedded_discounts")
//    private String embeddedDiscounts;
//    @JsonProperty("embedded_items")
//    private String embeddedItems;
//    @JsonProperty("embedded_payments")
//    private String embeddedPayments;
//    @JsonProperty("embedded_voided_items")
//    private String embeddedVoidedItems;
//    @JsonProperty("embedded_service_charges")
//    private String embeddedServiceCharges;
//    @JsonProperty("totals_other_charges")
//    private String totalsOtherCharges;
//    @JsonProperty("totals_items")
//    private String totalsItems;
//    @JsonProperty("totals_tips")
//    private String totalsTips;
//    @JsonProperty("totals_discounts")
//    private String totalsDiscounts;
//    @JsonProperty("clover_order_id")
//    private String cloverOrderId;
//    @JsonProperty("clover_taxtype_id")
//    private String cloverTaxTypeId;
//    @JsonProperty("removed_in_clover")
//    private String removedInClover;
//    @JsonProperty("processed")
//    private String processed;
//    @JsonProperty("processed_datetime")
//    private String processedDateTime;
//    @JsonProperty("pickup_time")
//    private String pickupTime;
//    @JsonProperty("pickup_info")
//    private String pickupInfo;
//    @JsonProperty("last_ov_call_datetime")
//    private String lastOvCallDateTime;
//    @JsonProperty("created_on")
//    private String createdOn;
//    @JsonProperty("created_by")
//    private String createdBy;
//    @JsonProperty("created_datetime")
//    private String createdDateTime;
//    @JsonProperty("last_on")
//    private String lastOn;
//    @JsonProperty("last_by")
//    private String lastBy;
//    @JsonProperty("last_datetime")
//    private String lastDateTime;
//    @JsonProperty("tips")
//    private String tips;
//    @JsonProperty("total_payments")
//    private String totalPayments;
//    @JsonProperty("due1")
//    private String due1;
//    @JsonProperty("status")
//    private String status;
//    @JsonProperty("canAddTip")
//    private String canAddTip;
//    @JsonProperty("canPayWhenClose")
//    private String canPayWhenClose;
//
//    /**
//     * Ticket Item []
//     */
//
//    @JsonProperty("omnivore_tickets_items_id")
//    private String omnivoreTicketsItemsId;
//    @JsonProperty("location_id")
//    private String locationId;
//    @JsonProperty("omnivore_location_id")
//    private String omnivoreLocationId;
//    @JsonProperty("ticket_id")
//    private String ticketId;
//    @JsonProperty("opened_at")
//    private String openedAt;
//    @JsonProperty("sent_time")
//    private String sentTime;
//    @JsonProperty("category_id")
//    private String categoryId;
//    @JsonProperty("menu_item_id")
//    private String menuItemId;
//    @JsonProperty("sp_internal_promotion")
//    private String spInternalPromotion;
//    @JsonProperty("combo_id")
//    private String comboId;
//    @JsonProperty("item_id")
//    private String itemId;
//    @JsonProperty("item_name")
//    private String itemName;
//    @JsonProperty("item_comment")
//    private String itemComment;
//    @JsonProperty("price_level")
//    private String priceLevel;
//    @JsonProperty("original_amount")
//    private String originalAmount;
//    @JsonProperty("price_per_unit")
//    private String pricePerUnit;
//    @JsonProperty("quantity")
//    private String quantity;
//    @JsonProperty("qty_split")
//    private String quantitySplit;
//    @JsonProperty("aireus_quantity")
//    private String aireusQuantity;
//    @JsonProperty("item_total")
//    private String itemTotal;
//    @JsonProperty("void_item_id")
//    private String voidItemId;
//    @JsonProperty("is_void")
//    private String isVoid;
//    @JsonProperty("sent")
//    private String sent;
//    @JsonProperty("split")
//    private String split;
//    @JsonProperty("price")
//    private String price;
//    @JsonProperty("seat")
//    private String seat;
//    @JsonProperty("clover_item_id")
//    private String cloverItemId;
//    @JsonProperty("omnivore_tickets_id")
//    private String omnivoreTicketsId;
//    @JsonProperty("")
//    @JsonProperty("")
//    @JsonProperty("")
//    @JsonProperty("")
//
//
//
//
//
//}
