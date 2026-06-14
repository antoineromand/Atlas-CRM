import { Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ClientContactEditorFacade } from '../../services/client-contact-editor.facade';

@Component({
  selector: 'app-client-contact-editor-drawer',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './client-contact-editor-drawer.component.html',
  styleUrl: './client-contact-editor-drawer.component.scss',
})
export class ClientContactEditorDrawerComponent {
  protected readonly clientContactEditor = inject(ClientContactEditorFacade);
}
