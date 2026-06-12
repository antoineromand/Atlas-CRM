import { Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ClientEditorFacade } from '../../services/client-editor.facade';

@Component({
  selector: 'app-client-editor-drawer',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './client-editor-drawer.component.html',
  styleUrl: './client-editor-drawer.component.scss',
})
export class ClientEditorDrawerComponent {
  protected readonly clientEditor = inject(ClientEditorFacade);
}
