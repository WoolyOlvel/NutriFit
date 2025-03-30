<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\Chat;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class chatController extends Controller
{
    public function index()
    {
        $chat = Chat::all();
        return response()->json([ 'chats' => $chat, 'status' => 200 ], 200);
    }

    public function store(Request $request)
{
    $validator = Validator::make($request->all(), [
        'name' => 'required',
        'message' => 'required',
        'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
        'time' => 'required|date',
        'read' => 'required',
        'isOnline' => 'required',
        'isCurrentUser' => 'required'
    ]);

    if ($validator->fails()) {
        return response()->json([
            'message' => 'Error en la validación de datos',
            'errors' => $validator->errors(),
            'status' => 400
        ], 400);
    }

    // Subir la imagen si existe
    $imagePath = null;
    if ($request->hasFile('image')) {
        $imagePath = $request->file('image')->store('images/chats', 'public');
    }

    // Convertir los valores booleanos a enteros (1 para true, 0 para false)
    $read = $request->has('read') ? ($request->read ? 1 : 0) : 0;
    $isOnline = $request->has('isOnline') ? ($request->isOnline ? 1 : 0) : 0;
    $isCurrentUser = $request->has('isCurrentUser') ? ($request->isCurrentUser ? 1 : 0) : 0;

    // Crear el nuevo chat
     $chat = Chat::create([
        'name' => $request->name,
        'message' => $request->message,
        'image' => $imagePath, // Guarda la ruta en la base de datos
        'time' => $request->time,
        'read' => $read, // Usar valores enteros para booleanos
        'isOnline' => $isOnline,
        'isCurrentUser' => $isCurrentUser,
    ]);

    return response()->json(['chat' => $chat, 'status' => 201], 201);
}



    public function show($id)
    {
        $chat = Chat::find($id);

        if (!$chat) {
            return response()->json(['message' => 'Chat no encontrado', 'status' => 404], 404);
        }

        return response()->json(['chat' => $chat, 'status' => 200], 200);
    }


    public function update(Request $request, $id)
    {
        $chat = Chat::find($id);

        if (!$chat) {
            return response()->json(['message' => 'Chat no encontrado', 'status' => 404], 404);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'message' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'time' => 'required|date',
            'read' => 'required',
            'isOnline' => 'required',
            'isCurrentUser' => 'required'
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Error en la validación de datos', 'errors' => $validator->errors(), 'status' => 400], 400);
        }

        if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/chats', 'public');
            $chat->image = $imagePath;
        }

        $chat->fill($request->except('image'));
        $chat->save();

        return response()->json(['chat' => $chat, 'message' => 'Chat actualizado', 'status' => 200], 200);
    }


    public function updatePartial(Request $request, $id)
    {
        $chat = Chat::find($id);

        if (!$chat) {
            return response()->json(['message' => 'Chat no encontrado', 'status' => 404], 404);
        }

        $chat->fill($request->only([ 'name', 'message', 'image', 'time', 'read', 'isOnline', 'isCurrentUser']));
        $chat->save();

        return response()->json(['chat' => $chat, 'message' => 'Chat actualizado parcialmente', 'status' => 200], 200);
    }

    public function destroy($id)
    {
        $chat = Chat::find($id);

        if (!$chat) {
            return response()->json(['message' => 'Chat no encontrado', 'status' => 404], 404);
        }

        $chat->delete();

        return response()->json(['message' => 'Chat eliminado', 'status' => 200], 200);
    }



}
