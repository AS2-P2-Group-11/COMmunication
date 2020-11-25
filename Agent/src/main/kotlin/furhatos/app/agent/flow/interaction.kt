package furhatos.app.communication.flow

import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.communication.nlu.*

//Greeting
val Start = state(Interaction) {
    onEntry {
        random(
                {   furhat.say("Whazzup dawg?!") },
                {   furhat.say("Greetings! What an absolute pleasure to meet you.") }
        )

        goto(ChooseAction)
    }
}

//The "main menu" when the user chooses what he want to do, e.g. place an order.
val ChooseAction = state(Interaction) {
    onEntry {
        furhat.ask("What can I help you with?")
    }
    //What happens on response nothing?

    //If the user wants to place an order
    onResponse<NewOrder> {
        //--->Create an empty shopping cart here<---
        goto(ChooseShoppingCartAction)
    }

    onResponse<ListOptions> {
        furhat.say {"You can place an order, change an order, or track an order"}
        reentry()
    }

    onResponse {
        furhat.say("I'm sorry, I didn't catch that.")
        furhat.say("You can place an order, change an order, or track an order")
        reentry()
    }
}


val ChooseShoppingCartAction = state(Interaction){
    onEntry {
        //Get ShoppingCart.items from API
        if (ShoppingCart.items.isEmpty()){
            furhat.say("Your shopping cart is currently empty")
        }
        else{
            furhat.say {
                +"Your shopping cart currently consist of"
                //Get ShoppingCart.items from API
                +println(ShoppingCart.items) }
        }
        furhat.ask("What do you want to do with it?")
    }

    onResponse<Checkout> {
        //Get ShoppingCart.items from API
        if(ShoppingCart.items.isEmpty()){
            furhat.say{"The shopping cart is empty and can not be checked out!"}
            furhat.say {"Your choices are to add items, or aborting the order"}
            reentry()
        }
        else{
            furhat.say {
                +"Your order has been placed and your order number is "
                //Get ShoppingCart.id from API
                +ShoppingCart.id
                +"Have a nice day!"
            }
            goto(Idle)
        }
    }
    onResponse<AddItems> {
        goto(ListShoppingItems)
    }
    onResponse<ChangeItem> {
        if (ShoppingCart.items.isEmpty()){
            furhat.say { "Your shopping cart is empty, you can't change an item in it." }
            reentry()
        }
        else {
            goto(ChooseItemToChange)
        }
    }
    onResponse<AbortOrdering> {
        furhat.say("Order aborted")
        goto(ChooseAction)
    }
    onResponse<ListOptions> {
        furhat.say {"Your choices are to checkout your order, add items, change items, or aborting the order"}
        reentry()
    }
    onResponse {
        furhat.say {"That is not an option, if you want you can ask me to list your options"
        }
        reentry()
    }
}

val ListShoppingItems = state(Interaction){
    onEntry {
        furhat.ask {"Do you want me to list the available items in the store?"}
    }

    onResponse<Yes> {
        furhat.say {
            +"The available items are"
            //Get Shop.items from API
            +Shop.Items
        }
        goto(AddShoppingItems)
    }

    onResponse<No> {
        goto(AddShoppingItems)
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

val AddShoppingItems = state(ChooseShoppingCartAction){
    onEntry {
        furhat.ask("What items do you want to add to the list?")
    }

    onResponse<StoreItem> {
        //Update ShoppingCart in the API
        val chosenItem = it.intent.itemsInStore
        goto(SpecifyQuantity(chosenItem))
    }
    onResponse<ListOptions> {
        furhat.say {"If you do not want to add more items, your choices are to checkout your order, change items, or aborting the order"}
        reentry()
    }
}

fun SpecifyQuantity(chosenItem: itemsInCart) : State=state(Interaction){
    onEntry {
        furhat.ask("How many"+chosenItem+" do you want to add to in your shopping cart?")
    }
    onResponse<IntegerNumber> {
        //Get the number from nlu
        val quantity=it.intent.integerNumber
        //Change quantity in API
        ShoppingCart.items.add.quantity=quantity
        goto(ChooseShoppingCartAction)
    }

    onResponse {
        furhat.say { "I did not catch that"}
        reentry()
    }
}

val ChooseItemToChange = state(ChooseShoppingCartAction){
    onEntry {
        furhat.ask("What item do you want to change or remove?")
    }
    onResponse<ItemInCart> {
        val itemToChange = it.intent.itemsInCart
        goto(SpecifyNewQuantity(itemToChange))
    }
    onResponse<ListOptions> {
        furhat.say {"If you do not want to change or remove an item, your choices are to checkout your order, add an item, or aborting the order"}
        reentry()
    }
}

fun SpecifyNewQuantity(itemToChange: itemsInCart) : State=state(Interaction){
    onEntry {
        furhat.ask("How many"+itemToChange+"Do you want to keep in your shopping cart?")
    }
    onResponse<IntegerNumber> {
        //Get the number from nlu
        val quantity=it.intent.integerNumber
        //Change quantity in API
        ShoppingCart.items(itemToChange).quantity=quantity
        goto(ChooseShoppingCartAction)
    }

    onResponse {
        furhat.say { "I did not catch that, you currently have "+ShoppingCart.items(itemToChange).quantity+" in your shopping cart" }
        reentry()
    }
}

