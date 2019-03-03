package tthttl.vetservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tthttl.vetservice.exception.ResourceNotFoundException;
import tthttl.vetservice.model.Vet;
import tthttl.vetservice.service.VetService;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vets")
public class VetController {

    private final VetService vetService;

    public VetController(VetService vetService) {
        this.vetService = vetService;
    }

    @PostMapping
    public ResponseEntity<Vet> saveVet(@Valid @RequestBody Vet vet) {
        Vet savedVet = vetService.save(vet);
        URI location = MvcUriComponentsBuilder
                .fromController(getClass())
                .path("/{id}")
                .buildAndExpand(savedVet.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedVet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vet> findById(@PathVariable Long id) {
        return vetService.findById(id)
                .map(vet -> ResponseEntity.ok().body(vet))
                .orElseThrow(() -> new ResourceNotFoundException(String.valueOf(id), getClass().toString()));
    }

    @GetMapping
    public ResponseEntity<List<Vet>> findAll() {
        return ResponseEntity.ok(vetService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vet> update(@PathVariable Long id,
                                      @Valid @RequestBody Vet vetToCopy) {
        return vetService.findById(id)
                .map(vetToUpdate -> {
                    Vet updatedVet = vetService.save(Vet.update(vetToUpdate, vetToCopy));
                    URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
                    return ResponseEntity.created(location).body(updatedVet);
                })
                .orElseThrow(() -> new ResourceNotFoundException(String.valueOf(id), getClass().toString()));
    }

}