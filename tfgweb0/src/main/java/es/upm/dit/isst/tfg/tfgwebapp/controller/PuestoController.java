package es.upm.dit.isst.tfg.tfgwebapp.controller;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
//import jakarta.activation.DataContentHandler;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import javax.mail.Authenticator;
//import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.MessagingException;
//import javax.mail.Session;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
//import javax.activation.DataContentHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.upm.dit.isst.tfg.tfgwebapp.model.Candidato;
import es.upm.dit.isst.tfg.tfgwebapp.model.Empleado;
import es.upm.dit.isst.tfg.tfgwebapp.model.Puesto;
import es.upm.dit.isst.tfg.tfgwebapp.model.Puesto2;

@Configuration
@EnableAsync
@Controller
public class PuestoController {
    public final String PUESTOMANAGER_STRING = "http://localhost:8083/puestos/";
    public final String CANMANAGER_STRING = "http://localhost:8083/candidatos/";
    public final String RHMANAGERGER_STRING = "http://localhost:8083/empleados/";

    public static final String VISTA_LISTA_PUESTO = "lista_puestos";
    public static final String VISTA_FORMULARIO_PUESTO = "form_puesto";
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("puestos/lista")
    public String lista(Model model, Principal principal) {
        List<Puesto> lista = new ArrayList<Puesto>();
        lista = Arrays.asList(restTemplate.getForEntity(PUESTOMANAGER_STRING + "libres/",
                Puesto[].class).getBody());
        model.addAttribute("puesto", lista);
        return VISTA_LISTA_PUESTO;
    }

    @GetMapping("IPS")
    public String ips(Map<String, Object> model) {
        List<Puesto> ops = new ArrayList<Puesto>();
        ops = Arrays.asList(restTemplate.getForEntity(PUESTOMANAGER_STRING,
                Puesto[].class).getBody());
        model.put("ops", ops);

        Puesto puesto = new Puesto();
        Puesto2 puesto2 = new Puesto2();

        model.put("puesto", puesto);
        model.put("puesto2", puesto2);

        model.put("accion", "procesos/guardar");
        model.put("accion2", "puestos/guardar2");

        return "IPS";
    }

    @GetMapping("procesos")
    public String procesos(Map<String, Object> model) {
        List<Puesto> puestos = new ArrayList<Puesto>();
        puestos = Arrays.asList(restTemplate.getForEntity(PUESTOMANAGER_STRING + "libres",
                Puesto[].class).getBody());
        model.put("puesto", puestos);

        // model.put("accion", "puestos/guardar");
        return "procesos";
    }

    @GetMapping("procesos/seleccioni/{idpuesto}")
    public String seleccioni(@PathVariable(value = "idpuesto") Integer idpuesto, Map<String, Object> model) {

        List<Candidato> candidatos = new ArrayList<Candidato>();
        List<Candidato> candidatos2 = new ArrayList<Candidato>();

        try {
            Puesto puesto1 = restTemplate.getForObject(PUESTOMANAGER_STRING + idpuesto, Puesto.class);
            candidatos = Arrays.asList(restTemplate.getForEntity("http://localhost:8083/candidatos/",
                    Candidato[].class).getBody());

            if (puesto1 != null) {
                System.out.println(puesto1.getNombre());
                for (Candidato s : candidatos) {
                    System.out.println(s.getpuesto());
                    if (s.getpuesto().equals(puesto1.getNombre())
                            && (s.getpreseleccionado().equals("0") || s.getpreseleccionado().equals("1"))) {
                        System.out.println("yes");
                        candidatos2.add(s);
                    }
                }
            } else {
                return "403";
            }
            model.put("candidato", candidatos2);
            model.put("ip", idpuesto);
            model.put("puesto", puesto1);
            return "seleccioni";
        } catch (Error e) {
            return "403";
        }
    }

    @GetMapping("procesos/seleccionf/{idpuesto}")
    public String seleccionf(@PathVariable(value = "idpuesto") Integer idpuesto, Map<String, Object> model) {

        List<Candidato> candidatos = new ArrayList<Candidato>();
        List<Candidato> candidatos2 = new ArrayList<Candidato>();

        try {
            Puesto puesto1 = restTemplate.getForObject(PUESTOMANAGER_STRING + idpuesto, Puesto.class);
            candidatos = Arrays.asList(restTemplate.getForEntity("http://localhost:8083/candidatos/",
                    Candidato[].class).getBody());

            if (puesto1 != null) {
                System.out.println(puesto1.getNombre());
                for (Candidato s : candidatos) {
                    System.out.println(s.getpuesto());
                    if (s.getpuesto().equals(puesto1.getNombre()) && s.getpreseleccionado().equals("1")) {
                        System.out.println("yes2");
                        candidatos2.add(s);
                    }
                }
            } else {
                return "403";
            }
            model.put("candidato", candidatos2);
            model.put("ip", idpuesto);

            return "seleccionf";
        } catch (Error e) {
            return "403";
        }
    }

    @GetMapping("puestos/crear")
    public String crear(Map<String, Object> model) {
        List<Puesto> lista = new ArrayList<Puesto>();
        lista = Arrays.asList(restTemplate.getForEntity(PUESTOMANAGER_STRING + "libres/",
                Puesto[].class).getBody());
        Puesto puesto = new Puesto();
        model.put("puesto", puesto);
        model.put("accion", "guardar");

        return VISTA_FORMULARIO_PUESTO;
    }

    @GetMapping("procesos/contratar/{ip}/{ipc}")
    public String contratar(@PathVariable Integer ip, @PathVariable String ipc,
            Map<String, Object> model) {
        Empleado emp = new Empleado();
        Candidato c = new Candidato();
        try {

            c = restTemplate.getForObject("http://localhost:8083/candidatos/" + ipc, Candidato.class);
        } catch (Error e) {

            return "seleccionf";
        }
        model.put("Empleado", emp);
        model.put("accion", "guardar");// "procesos/contratar/" + ip + "/" + ipc + "/guardar");
        model.put("date", ZonedDateTime.now());
        model.put("candidato", c);
        model.put("ip", ip);
        model.put("ipc", ipc);
        // model.put("result", result.getAllErrors());
        return "contratar";
    }

    @PostMapping("procesos/contratar/{ip}/{ipc}/guardar")
    public String contratarg(@Validated Empleado Empleado, BindingResult result, @PathVariable Integer ip,
            @PathVariable String ipc, Map<String, Object> model, Principal principal) {
        Candidato c = new Candidato();
        Puesto p = new Puesto();
        if (result.hasErrors()) {
            try {

                c = restTemplate.getForObject("http://localhost:8083/candidatos/" + ipc, Candidato.class);
            } catch (Error e) {
                model.put("Empleado", Empleado);
                List<ObjectError> r = result.getAllErrors();
                // r.stream().map(a -> a);

                model.put("result", r);// .getDefaultMessage());
                model.put("candidato", c);

                model.put("date", ZonedDateTime.now());
                System.out.println("1holaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

                return "contratar";
            }
            model.put("Empleado", Empleado);
            List<ObjectError> r = result.getAllErrors();
            // r.stream().map(a -> a);

            model.put("result", r);// .getDefaultMessage());

            model.put("date", ZonedDateTime.now());
            model.put("candidato", c);

            // cerrar proceso y borrar candidato
            // System.out.println(r.get(0));

            // return "redirect:/procesos/contratar/" + ip + "/" + ipc;
            System.out.println("2holaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

            return "contratar";
        } else {
            try {
                p = restTemplate.getForObject("http://localhost:8083/puestos/" + ip, Puesto.class);

                c = restTemplate.getForObject("http://localhost:8083/candidatos/" + ipc, Candidato.class);

                restTemplate.postForObject(RHMANAGERGER_STRING, Empleado, Empleado.class);
                restTemplate.delete("http://localhost:8083/candidatos/" + ipc);
                restTemplate.postForObject(PUESTOMANAGER_STRING + ip + "/cerrar", p, Puesto.class);
                String remitente = "factorrhisst@gmail.com";
                // La clave de aplicación obtenida según se explica en este artículo:
                String claveemail = "tyrnsurboetzwbug";
                String destinatario = principal.getName(); // A quien le quieres escribir.
                String destinatario2 = Empleado.getEmail(); // A quien le quieres escribir.

                String asunto = "Fin de petición de candidatos para puesto: " + p.getNombre();
                String asunto2 = "Resultado de selección para el puesto: " + p.getNombre();

                String cuerpo = "Ya no se requieren candidatos para el puesto: " + c.getpuesto() + " con id: " + ip;
                String cuerpo2 = "Ha sido contratado para el puesto: " + c.getpuesto() + " con id: " + ip
                        + "\n Sus credenciales son \n\n Usuario: " + Empleado.getEmail() + "\nContraseña: "
                        + Empleado.getPassword();

                Properties props = System.getProperties();
                props.put("mail.smtp.host", "smtp.gmail.com"); // El servidor SMTP de Google
                props.put("mail.smtp.user", remitente);
                props.put("mail.smtp.clave", claveemail); // La clave de la cuenta
                props.put("mail.smtp.auth", "true"); // Usar autenticación mediante usuario y clave
                props.put("mail.smtp.starttls.enable", "true"); // Para conectar de manera segura al servidor SMTP
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.port", "587"); // El puerto SMTP seguro de Google
                props.put("mail.smtp.ssl.trust", "*");
                Session session = Session.getDefaultInstance(props);
                MimeMessage message = new MimeMessage(session);
                MimeMessage message2 = new MimeMessage(session);

                try {
                    message.setFrom(new InternetAddress(remitente));
                    message2.setFrom(new InternetAddress(remitente));

                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario)); // Se podrían
                                                                                                       // añadir
                                                                                                       // varios de la
                                                                                                       // misma
                                                                                                       // manera
                    message2.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario2)); // Se podrían

                    message.setSubject(asunto);
                    message2.setSubject(asunto2);

                    message.setText(cuerpo);
                    message2.setText(cuerpo2);

                    Transport transport = session.getTransport("smtp");

                    transport.connect("smtp.gmail.com", remitente, claveemail);

                    transport.sendMessage(message, message.getAllRecipients());
                    transport.sendMessage(message2, message2.getAllRecipients());

                    transport.close();
                } catch (MessagingException me) {
                    me.printStackTrace(); // Si se produce un error
                }

            } catch (Exception e) {
                model.put("date", ZonedDateTime.now());
                model.put("candidato", c);
                model.put("Empleado", Empleado);
                System.out.println(e);

                return "contratar";

            }

            return "redirect:/lista";
        }
    }

    @GetMapping("puestos/{ip}/cerrar")
    public String cerrar(@PathVariable Integer ip, Map<String, Object> model) {

        Puesto puesto = new Puesto();
        model.put("puesto", puesto);
        model.put("accion", "guardar");
        try {
            List<Puesto> lista = new ArrayList<Puesto>();

            lista = Arrays.asList(restTemplate.getForEntity(PUESTOMANAGER_STRING + "libres/",
                    Puesto[].class).getBody());

            restTemplate.postForObject(PUESTOMANAGER_STRING + ip + "/cerrar", puesto, Puesto.class);
        } catch (Exception e) {
            return "403";
        }

        return "redirect:/" + "procesos";
    }

    @PostMapping("puestos/guardar")
    public String guardar(@Validated Puesto puesto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("puesto", puesto);
            return VISTA_FORMULARIO_PUESTO;
        }
        try {
            List<Puesto> lista = new ArrayList<Puesto>();

            lista = Arrays.asList(restTemplate.getForEntity(PUESTOMANAGER_STRING + "libres/",
                    Puesto[].class).getBody());
            restTemplate.postForObject(PUESTOMANAGER_STRING, puesto, Puesto.class);
        } catch (Exception e) {
        }

        return "redirect:/" + "puestos/lista";
    }

    @PostMapping("puestos/guardar2")
    public String guardar2(@Validated Puesto2 puesto, Principal principal, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("puesto", puesto);
            return "IPS";
        }
        try {
            Puesto puestosend = new Puesto();
            List<Puesto> lista = new ArrayList<Puesto>();
            lista = Arrays
                    .asList(restTemplate.getForEntity(PUESTOMANAGER_STRING + "nombre/" + puesto.getNombre(),
                            Puesto[].class).getBody());
            System.out.println();
            puestosend.setEstado("0");
            puestosend.setDepto(lista.get(0).getDepto());
            puestosend.setNombre(lista.get(0).getNombre());
            puestosend.setDesc_puesto(lista.get(0).getDesc_puesto());
            // puestosend.setId_puesto("99");

            Puesto p = restTemplate.postForObject(PUESTOMANAGER_STRING, puestosend, Puesto.class);
            String remitente = "factorrhisst@gmail.com";
            String claveemail = "tyrnsurboetzwbug";
            String destinatario = principal.getName(); // Email reclutador.

            String asunto = "Petición de candidatos para puesto: " + p.getNombre();

            String cuerpo = "Se requieren candidatos para el puesto: " + p.getNombre() + " con id: "
                    + p.getId_puesto();

            Properties props = System.getProperties();
            props.put("mail.smtp.host", "smtp.gmail.com"); // El servidor SMTP de Google
            props.put("mail.smtp.user", remitente);
            props.put("mail.smtp.clave", claveemail); // La clave de la cuenta
            props.put("mail.smtp.auth", "true"); // Usar autenticación mediante usuario y clave
            props.put("mail.smtp.starttls.enable", "true"); // Para conectar de manera segura al servidor SMTP
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.port", "587"); // El puerto SMTP seguro de Google
            props.put("mail.smtp.ssl.trust", "*");
            Session session = Session.getDefaultInstance(props);
            MimeMessage message = new MimeMessage(session);

            try {
                message.setFrom(new InternetAddress(remitente));

                message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario)); // Se podrían
                                                                                                   // añadir
                                                                                                   // varios de la
                                                                                                   // misma
                                                                                                   // manera
                message.setSubject(asunto);

                message.setText(cuerpo);

                Transport transport = session.getTransport("smtp");
                transport.connect("smtp.gmail.com", remitente, claveemail);

                transport.sendMessage(message, message.getAllRecipients());
                transport.close();

            } catch (MessagingException me) {
                me.printStackTrace(); // Si se produce un error
            }

        } catch (Exception e) {
            model.addAttribute("puesto", puesto);
            System.out.println(e.getMessage());
            return "IPS";

        }
        model.addAttribute("puesto", puesto);

        return "redirect:/" + "procesos";
    }

    @PostMapping("procesos/guardar")
    public String procesosguardar(Principal principal, @Validated Puesto puesto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("puesto", puesto);
            return "IPS";
        }
        try {
            restTemplate.postForObject(PUESTOMANAGER_STRING, puesto, Puesto.class);

            String remitente = "factorrhisst@gmail.com";
            String claveemail = "tyrnsurboetzwbug";
            String destinatario = principal.getName(); // Email reclutador.

            String asunto = "Petición de candidatos para puesto: " + puesto.getNombre();

            String cuerpo = "Se requieren candidatos para el puesto: " + puesto.getNombre();

            Properties props = System.getProperties();
            props.put("mail.smtp.host", "smtp.gmail.com"); // El servidor SMTP de Google
            props.put("mail.smtp.user", remitente);
            props.put("mail.smtp.clave", claveemail); // La clave de la cuenta
            props.put("mail.smtp.auth", "true"); // Usar autenticación mediante usuario y clave
            props.put("mail.smtp.starttls.enable", "true"); // Para conectar de manera segura al servidor SMTP
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.port", "587"); // El puerto SMTP seguro de Google
            props.put("mail.smtp.ssl.trust", "*");
            Session session = Session.getDefaultInstance(props);
            MimeMessage message = new MimeMessage(session);

            try {
                message.setFrom(new InternetAddress(remitente));

                message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario)); // Se podrían
                                                                                                   // añadir
                                                                                                   // varios de la
                                                                                                   // misma
                                                                                                   // manera
                message.setSubject(asunto);

                message.setText(cuerpo);

                Transport transport = session.getTransport("smtp");

                transport.connect("smtp.gmail.com", remitente, claveemail);
                transport.sendMessage(message, message.getAllRecipients());

                transport.close();
            } catch (MessagingException me) {
                me.printStackTrace(); // Si se produce un error
            }
        } catch (Exception e) {
        }
        return "redirect:/" + "procesos";
    }

    @GetMapping("procesos/pasaruno/{ip}/{ipc}")
    public String procesospasaruno(@Validated Candidato candidato, @PathVariable String ipc, @PathVariable Integer ip,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            // model.addAttribute("candidato", candidato);
            model.addAttribute("result", result.getAllErrors().toString());
            System.out.println(result.getAllErrors().toString());

            return "403";
        }
        try {
            candidato = new Candidato();
            candidato = restTemplate.getForObject("http://localhost:8083/candidatos/" + ipc,
                    Candidato.class);
            System.out.println();

            // restTemplate.postForObject(PUESTOMANAGER_STRING, candidato, Candidato.class);
            restTemplate.postForObject("http://localhost:8083/candidatos/select/" + ipc, candidato, Candidato.class);

            System.out.println("buenas:  ");

            return "redirect:/procesos/seleccioni/" + ip;

        } catch (Exception e) {
            System.out.println("malas:  ");
            System.out.println(e);

            return "403";

        }
    }

    @GetMapping("procesos/quitaruno/{ip}/{ipc}")
    public String procesosquitaruno(@Validated Candidato candidato, @PathVariable String ipc, @PathVariable Integer ip,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            // model.addAttribute("candidato", candidato);
            model.addAttribute("result", result.getAllErrors().toString());
            System.out.println(result.getAllErrors().toString());

            return "403";
        }
        try {
            candidato = new Candidato();
            candidato = restTemplate.getForObject("http://localhost:8083/candidatos/" + ipc,
                    Candidato.class);
            System.out.println();

            // restTemplate.postForObject(PUESTOMANAGER_STRING, candidato, Candidato.class);
            restTemplate.postForObject("http://localhost:8083/candidatos/deselect/" + ipc, candidato, Candidato.class);

            System.out.println("buenas:  ");

            return "redirect:/procesos/seleccioni/" + ip;

        } catch (Exception e) {
            System.out.println("malas:  ");
            System.out.println(e);

            return "403";

        }
    }

    @GetMapping("procesos/pasartodos/{ip}")

    public String procesospasartodos(Puesto puesto, @PathVariable Integer ip,
            BindingResult result,
            Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("result", result.getAllErrors().toString());
            System.out.println(result.getAllErrors().toString());

            return "seleccioni";
        }
        try {

            puesto = restTemplate.getForObject(PUESTOMANAGER_STRING + ip, Puesto.class);
            restTemplate.put(PUESTOMANAGER_STRING + ip + "/pasar", puesto, Puesto.class);

            System.out.println("buenas:  ");
            ra.addFlashAttribute("success", "Candidato seleccionado, está disponible en selección final");
            return "redirect:/procesos/seleccionf/" + ip;

        } catch (Exception e) {
            System.out.println(e);

            return "seleccioni";

        }
    }

    @GetMapping("puestos/editar/{id}")
    public String editar(@PathVariable(value = "id") Integer id,
            Map<String, Object> model, Principal principal) {
        // if (principal == null || ! principal.getName().equals(id))
        // return "redirect:/puestos/lista";
        Puesto puesto = null;
        try {
            puesto = restTemplate.getForObject(PUESTOMANAGER_STRING + id, Puesto.class);
        } catch (HttpClientErrorException.NotFound ex) {
        }
        model.put("puesto", puesto);
        model.put("accion", "actualizar");
        return puesto != null ? VISTA_FORMULARIO_PUESTO : "redirect:/puestos/lista";
    }

    @PostMapping("puestos/editar/actualizar")
    public String actualizar(@Validated Puesto puesto, BindingResult result) {
        if (result.hasErrors()) {
            return VISTA_FORMULARIO_PUESTO;
        }
        try {
            restTemplate.put(PUESTOMANAGER_STRING + puesto.getId_puesto(),
                    puesto, Puesto.class);
        } catch (Exception e) {
        }
        return "redirect:/puestos/lista";
    }

    @GetMapping("puestos/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Integer id) {
        restTemplate.delete(PUESTOMANAGER_STRING + id);
        return "redirect:/puestos/lista";
    }

}
