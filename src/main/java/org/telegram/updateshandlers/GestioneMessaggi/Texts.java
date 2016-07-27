package org.telegram.updateshandlers.GestioneMessaggi;

import eu.trentorise.smartcampus.mobilityservice.model.TaxiContact;
import eu.trentorise.smartcampus.mobilityservice.model.TripData;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.List;

public class Texts {

    // region commands

    public static String textLanguage(Language language) {
        switch (language) {
            case ITALIANO:
                return "Cambia lingua";
            case ENGLISH:
                return "Change language";
            case ESPAÑOL:
                return "Cambia idioma";
            default:
                return "Impossible";
        }
    }

    public static String textStart(Language language) {
        switch (language) {
            case ITALIANO:
                return "*Benvenuto a @ViaggiaTrentoBot*\n" +
                        "Il bot che supporta la mobilità urbana sostenibile di Trento\n" +
                        "• Monitora i tuoi viaggi e sii informato riguardo eventuali ritardi\n" +
                        "• Controlla in tempo reale gli orari di treni locali e bus urbani ed extraurbani\n" +
                        "• Controlla in tempo reale parcheggi disponibili nella città\n" +
                        "• Trova punti bici disponibili nella città\n" +
                        "\n" +
                        "*Per iniziare...*\n" +
                        "`TAXI` - restituisce informazioni utili riguardo i taxi di Trento\n" +
                        "`AUTOBUS` - restituisce gli orari dell'autobus richiesto\n" +
                        "`TRAINS` - restituisce gli orari del treno richiesto\n" +
                        "`PARKINGS` - restituisce la disponibilità e la posizione di parcheggi nelle vicinanze\n" +
                        "`BIKESHARINGS` - restituisce la disponibilità e la posizione di punti bici nelle vicinanze\n" +
                        "\n" +
                        "*Lista comandi*\n" +
                        "/start - ritorna questo messaggio\n" +
                        "/language - cambia la lingua del bot\n" +
                        "/help - restituisce messaggi d'aiuto nelle diverse sezioni\n";
            case ENGLISH:
                return "Welcome to @ViaggiaTrentoBot\n" +
                        "The bot which support sustainable urban mobility in Trento\n" +
                        "• Monitor your trips and be informed of any delays on the route\n" +
                        "• Check in real time the local train and bus timetables\n" +
                        "• Check in real time parking availability around the city\n" +
                        "\n" +
                        "*To begin...*\n" +
                        "`TAXI` - returns useful information about Trento's taxi service\n" +
                        "`BUS` - returns the timetable of the selected bus route\n" +
                        "`TRAINS` - returns the timetable of the selected train\n" +
                        "`PARKINGS`- returns the availability and the location of the car parks in the area\n" +
                        "`BIKESHARING`- returns the availability and the location of the bike-sharing service's points in the area\n" +
                        "\n" +
                        "*Command list*\n" +
                        "/start: return this message\n" +
                        "/stop: stop the bot, re-starts with the start command\n" +
                        "/language: change bot's language settings\n" +
                        "/help: returns tips";
            case ESPAÑOL:
                return "*Bienvenido en @ViaggiaTrentoBot*\n" +
                        "El bot que soporta la movilización  urbana sostenible de Trento\n" +
                        "• Podrás monitorear tus viajes y ser informado sobre eventuales retrasos\n" +
                        "• Controla en tiempo real los horarios de trenes locales, bus urbanos y extra-urbanos\n" +
                        "• Controla en tiempo real los aparcamientos disponibles en la ciudad\n" +
                        "• Encuentra bike sharing disponibles en la ciudad\n" +
                        "\n" +
                        "*Para iniciar...*\n" +
                        "`TAXI` -  Da información útil sobre los taxis de Trento\n" +
                        "`AUTOBUS` -  Da información de los horarios del bus seleccionado\n" +
                        "`TRAINS` - Da información de los horarios del tren seleccionado\n" +
                        "`PARKINGS` - Da la disponibilidad y la posición del aparcamiento más cercano\n" +
                        "`BIKESHARINGS` - Da la disponibilidad y la posición del bike sharing más cercano\n" +
                        "\n" +
                        "Lista de comandos*\n" +
                        "/start - regresa este mensaje\n" +
                        "/languege - cambia el idioma del bot\n" +
                        "/help - da mensajes de ayuda en las diferentes secciones";
            default:
                return "Impossible";
        }
    }

    public static String textError(Language language) {
        switch (language) {
            case ITALIANO:
                return "Alquanto inaspettato...";
            case ENGLISH:
                return "Quite unexpected...";
            case ESPAÑOL:
                return "Bastante inesperado";
            default:
                return "Impossible";
        }
    }

    public static String textOption(Language language) {
        switch (language) {
            case ITALIANO:
                return "Scusa, cosa hai detto?";
            case ENGLISH:
                return "Sorry, what did you say?";
            case ESPAÑOL:
                return "disculpa, ¿que has dicho?";
            default:
                return "Impossible";
        }
    }

    // endregion commands

    // region Menu.START

    public static String textStartHelp(Language language) {
        return textStart(language);
    }

    public static String textStartMain(Language language) {
        switch (language) {
            case ITALIANO:
                return "Dimmi cosa ti interessa sapere";
            case ENGLISH:
                return "Tell me what you want to know";
            case ESPAÑOL:
                return "Dime que quieres saber";
            default:
                return "Impossible";
        }
    }

    public static String textStartTaxi(List<TaxiContact> taxi) {
        String text;
        text = "*TAXI*";

        for (TaxiContact el : taxi) {
            text += "\n*" + el.getName() + "*";
            for (String st : el.getPhone())
                text += "\n" + st;
            for (String st : el.getSms())
                text += st.equals("") ? "" : "\n" + st;
        }

        return text;
    }

    public static String textStartAutobus(Language language) {
        switch (language) {
            case ITALIANO:
                return "Scegli la linea";
            case ENGLISH:
                return "Choose the line";
            case ESPAÑOL:
                return "Elige la linea";
            default:
                return "Impossible";
        }
    }

    public static String textStartTrains(Language language) {
        switch (language) {
            case ITALIANO:
                return "Scegli la linea";
            case ENGLISH:
                return "Choose the line";
            case ESPAÑOL:
                return "Elige la linea";
            default:
                return "Impossible";
        }
    }

    public static String textStartParkings(Language language) {
        switch (language) {
            case ITALIANO:
                return "Scegli il parcheggio, o invia la tua posizione";
            case ENGLISH:
                return "Choose the parking, or send your location";
            case ESPAÑOL:
                return "Elige el aparcamiento, o envia tu posición";
            default:
                return "Impossible";
        }
    }

    public static String textStartBikeSharings(Language language) {
        switch (language) {
            case ITALIANO:
                return "Scegli il bike sharing desiderato, o invia la tua posizione";
            case ENGLISH:
                return "Choose the bike sharing, or send your location";
            case ESPAÑOL:
                return "Elige el bike sharing, o envia tu posición";
            default:
                return "Impossible";
        }
    }

    // endregion Menu.START

    // region Menu.LANGUAGE

    public static String textLanguageChange(Language language) {
        switch (language) {
            case ITALIANO:
                return "Mi capisci ora?";
            case ENGLISH:
                return "Do you understand me now?";
            case ESPAÑOL:
                return "¿Me entiendes ahora?";
            default:
                return "Impossible";
        }
    }

    // endregion Menu.LANGUAGE

    // region Menu.AUTOBUS

    public static String textAutobusHelp(Language language) {
        switch (language) {
            case ITALIANO:
                return "Seleziona la linea desiderata per conoscere gli orari e le fermate nelle tue vicinanze.\n" +
                        "La barra in basso permette di scorrere le fasce orarie";
            case ENGLISH:
                return "Select a line to view the timetable and the stops in your area.\n" +
                        "The bar allows to select a specific time slot";
            case ESPAÑOL:
                return "Selecciona la linea deseada para conocer los horarios y la parada más cercanas.";
            default:
                return "Impossible";
        }
    }

    public static String textAutobus(List<TripData> tripDatas) {
        String text = "*AUTOBUS*";
        for (TripData el : tripDatas) {
            text += "\n" + el.getRouteName() + " " + DurationFormatUtils.formatDurationHMS(el.getTime());
        }

        return text;
    }

    // endregion Menu.AUTOBUS

    // region Menu.TRAINS

    public static String textTrainHelp(Language language) {
        switch (language) {
            case ITALIANO:
                return "Seleziona il treno desiderato per conoscere gli orari e le stazioni nelle tue vicinanze.\n" +
                        "La barra in basso permette di scorrere le fasce orarie";
            case ENGLISH:
                return "Select a train to view its timetable and the stations in your area\n" +
                        "The bar allows to select a specific time slot";
            case ESPAÑOL:
                return "Selecciona el tren deseado para conocer los horaros y las estaciones más cercanas\n" +
                        "La barra de abajo permite de mover la banda horaria";
            default:
                return "Impossible";
        }
    }

    public static String textTrain(List<Stop> stop) {
        String text = "*TRAIN*";
        for (Stop el : stop) {
            text += "\n" + el.getName() + " " + el.getLongitude();
        }
        return text;
    }


    // endregion Menu.TRAINS

    // region Menu.PARKINGS

    public static String textParkingsHelp(Language language) {
        switch (language) {
            case ITALIANO:
                return "Seleziona il parcheggio desiderato per conoscere la disponibilità e la sua posizione.\n" +
                        "Inoltre puoi inviare la tua posizione per trovare i parcheggi piu vicini";
            case ENGLISH:
                return "Select a parking to see if it is available and find its location.\n" +
                        "What's more, you can send your location to know near parkings";
            case ESPAÑOL:
                return "Selecciona el aparcamiento deseado para conocer la disponibilidad y su posición\n" +
                        "Además podrás enviar tu posición para encontrar el aparcamiento más cercano";
            default:
                return "Impossible";
        }
    }

    public static String textParking(Parking parking) {
        String slots = parking.isMonitored() ? "Slots available " + parking.getSlotsAvailable() : "Total Slots " + parking.getSlotsTotal();
        return "*Parking " + parking.getName() + "*\n" + parking.getDescription() + "\n" + slots;
    }

    public static String textParkingsNear(List<Parking> parkings) {
        String text = "*NEAR TO YOU*";
        for (Parking el : parkings) {
            text += "\n/" + el.getName() + " - " + el.getDescription();
        }
        if (parkings.isEmpty())
            text = "*NO PARKINGS NEAR TO YOU*";
        return text;
    }

    // endregion Menu.PARKINGS

    // region Menu.BIKESHARINGS

    public static String textBikeSharingsHelp(Language language) {
        switch (language) {
            case ITALIANO:
                return "Seleziona il punto bici desiderato per conoscere la disponibilità e la sua posizione.\n" +
                        "Inoltre puoi inviare la tua posizione per trovare i punti bici piu vicini";
            case ENGLISH:
                return "Select a bike sharing to see if it is available and find its location.\n" +
                        "What's more, you can send your location to know bike sharings";
            case ESPAÑOL:
                return "Selecciona el bike sharing deseado para conocer la disponibilidad y su posición\n" +
                        "Además podrás enviar tu posición para encontrar el bike sharing más cercano ";
            default:
                return "Impossible";
        }
    }

    public static String textBikeSharings(Parking parking) {
        String slots = parking.isMonitored() ? "Slots available " + parking.getSlotsAvailable() : "Total Slots " + parking.getSlotsTotal();
        return "*Bike sharing " + parking.getName() + "*\n" + parking.getDescription() + "\n" + slots;
    }

    public static String textBikeSharingsNear(List<Parking> parkings) {
        String text = "*NEAR TO YOU*";
        for (Parking el : parkings) {
            text += "\n" + el.getName() + " - " + el.getDescription();
        }
        if (parkings.isEmpty())
            text = "*NO BIKE SHARING NEAR TO YOU*";
        return text;
    }

    // endregion Menu.BIKESHARINGS

}
