package tthttl.vetservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tthttl.vetservice.exception.ResourceNotFoundException;
import tthttl.vetservice.model.Specialty;
import tthttl.vetservice.model.Vet;
import tthttl.vetservice.service.SpecialtyService;
import tthttl.vetservice.service.VetService;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vets")
public class VetController {

    private final VetService vetService;
    private final SpecialtyService specialtyService;

    public VetController(VetService vetService, SpecialtyService specialtyService) {
        this.vetService = vetService;
        this.specialtyService = specialtyService;
    }

    @PostMapping
    public ResponseEntity<Vet> saveVet(@Valid @RequestBody Vet vet) {
        saveSpecialties(vet);
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
                    saveSpecialties(vetToCopy);
                    Vet updatedVet = vetService.save(Vet.update(vetToUpdate, vetToCopy));
                    URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
                    return ResponseEntity.created(location).body(updatedVet);
                })
                .orElseThrow(() -> new ResourceNotFoundException(String.valueOf(id), getClass().toString()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return vetService.findById(id)
                .map(vetToDelete -> {
                    vetService.delete(vetToDelete);
                    return ResponseEntity.noContent().build();
                })
                .orElseThrow(() -> new ResourceNotFoundException(String.valueOf(id), getClass().toString()));
    }

    @GetMapping("/specialties")
    public ResponseEntity<List<Specialty>> findAllSpecialties() {
        return ResponseEntity.ok().body(specialtyService.findAll());
    }

    private void saveSpecialties(Vet vetToCopy){
        Set<Specialty> savedSpecialties = vetToCopy.getSpecialties().stream()
                .map(specialty -> {
                    if (specialty.getId() == null) {
                        return specialtyService.save(specialty);
                    }
                    return specialty;
                })
                .collect(Collectors.toSet());
        vetToCopy.setSpecialties(savedSpecialties);
    }

}
