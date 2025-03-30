<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\Patient;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class patientController extends Controller
{
    public function index()
    {
        $patient = Patient::all();
        return response()->json([ 'patient' => $patient, 'status' => 200 ], 200);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'age' => 'required',
            'phone' => 'required|date',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'gender' => 'required',
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
            $imagePath = $request->file('image')->store('images/patients', 'public'); 
         }

         $patient = Patient::create([
            'name' => $request->name,
            'age' => $request->age,
            'phone' => $request->phone,
            'image' => $imagePath, // Guarda la ruta en la base de datos
            'gender' => $request->gender,
        ]);

        return response()->json(['patient' => $patient, 'status' => 201], 201);
    }


    public function show($id)
    {
        $patient = Patient::find($id);

        if (!$patient) {
            return response()->json(['message' => 'Paciente no encontrado', 'status' => 404], 404);
        }

        return response()->json(['patient' => $patient, 'status' => 200], 200);
    }

    public function update(Request $request, $id)
    {
        $patient = Patient::find($id);

        if (!$patient) {
            return response()->json(['message' => 'Paciente no encontrado', 'status' => 404], 404);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'age' => 'required',
            'phone' => 'required|date',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'gender' => 'required',
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Error en la validación de datos', 'errors' => $validator->errors(), 'status' => 400], 400);
        }

        if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/appointments', 'public');
            $patient->image = $imagePath;
        }

        $patient->fill($request->except('image'));
        $patient->save();

        return response()->json(['patient' => $patient, 'message' => 'Paciente actualizado', 'status' => 200], 200);
    }

    public function updatePartial(Request $request, $id)
    {
        $patient = Patient::find($id);

        if (!$patient) {
            return response()->json(['message' => 'Paciente no encontrado', 'status' => 404], 404);
        }

        $patient->fill($request->only([ 'name', 'age', 'phone', 'image', 'gender']));
        $patient->save();

        return response()->json(['patient' => $patient, 'message' => 'Paciente actualizado parcialmente', 'status' => 200], 200);
    }

    public function destroy($id)
    {
        $patient = Patient::find($id);

        if (!$patient) {
            return response()->json(['message' => 'Paciente no encontrado', 'status' => 404], 404);
        }

        $patient->delete();

        return response()->json(['message' => 'Paciente eliminado', 'status' => 200], 200);
    }
}
